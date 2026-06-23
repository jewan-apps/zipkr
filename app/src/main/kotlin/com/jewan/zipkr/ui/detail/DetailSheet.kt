package com.jewan.zipkr.ui.detail

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewan.zipkr.BuildConfig
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.components.CopyBar
import com.jewan.zipkr.ui.components.CopyField
import com.jewan.zipkr.ui.components.KakaoMapWebView
import com.jewan.zipkr.ui.components.MapDeepLinkButtons
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.copyToClipboard
import com.jewan.zipkr.util.lightHaptic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val SHEET_RADIUS = 24.dp
private val MAP_HEIGHT = 220.dp
private val PLACEHOLDER_RADIUS = 16.dp

/**
 * 검색 결과 카드 탭 시 노출되는 상세 모달 시트이다.
 * skipPartiallyExpanded로 풀 높이까지 즉시 확장한다 (시·도 시트와 동일 패턴).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSheet(
    address: Address,
    onDismiss: () -> Unit,
    query: String = "",
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val coordinatePhase by viewModel.coordinate.collectAsState()

    LaunchedEffect(address.roadAddress) {
        viewModel.fetchCoordinate(address.roadAddress)
        // v1.1k: 시트 진입을 "최근 본 주소" 누적 시그널로 사용한다.
        viewModel.rememberRecent(address)
    }

    val isFavorite by viewModel.isFavoriteFlow(address).collectAsState(initial = false)
    val labels = rememberCopyLabels()
    val copy = rememberCopyHandler(onCopyPostalCode = viewModel::trackCopyPostalCode)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = SHEET_RADIUS, topEnd = SHEET_RADIUS),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { ClickableDragHandle(scope = scope, sheetState = sheetState, onDismiss = onDismiss) },
    ) {
        DetailSheetContent(
            address = address,
            query = query,
            coordinatePhase = coordinatePhase,
            lastCopied = copy.lastCopied,
            isFavorite = isFavorite,
            onFavoriteToggle = { viewModel.toggleFavorite(address) },
            onRetry = { viewModel.fetchCoordinate(address.roadAddress) },
            onCopy = copy.onCopy,
            labels = labels,
        )
    }
}

private const val HIGHLIGHT_DURATION_MS = 800L

@Composable
private fun rememberCopyLabels(): CopyLabels =
    CopyLabels(
        zip = stringResource(R.string.copy_zip_label),
        road = stringResource(R.string.copy_road_label),
        jibun = stringResource(R.string.copy_jibun_label),
        english = stringResource(R.string.copy_english_label),
    )

/**
 * 클립보드 복사 + 햅틱 + 토스트 + lastCopied auto-clear를 묶은 hook.
 * lastCopied는 HIGHLIGHT_DURATION_MS 이후 null로 자동 해제되어 본문 강조가 잠깐만 유지된다.
 */
@Composable
private fun rememberCopyHandler(onCopyPostalCode: () -> Unit): CopyHandler {
    val context = LocalContext.current
    val view = LocalView.current
    val toastTpl = stringResource(R.string.copy_toast)
    var lastCopied by remember { mutableStateOf<CopyField?>(null) }
    LaunchedEffect(lastCopied) {
        if (lastCopied != null) {
            kotlinx.coroutines.delay(HIGHLIGHT_DURATION_MS)
            lastCopied = null
        }
    }
    val onCopy: (CopyField, String, String) -> Unit = { field, label, text ->
        context.copyToClipboard(label, text)
        view.lightHaptic()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, toastTpl.format(text), Toast.LENGTH_SHORT).show()
        }
        if (field == CopyField.Zip) onCopyPostalCode()
        lastCopied = field
    }
    return CopyHandler(lastCopied, onCopy)
}

private data class CopyHandler(
    val lastCopied: CopyField?,
    val onCopy: (CopyField, String, String) -> Unit,
)

/**
 * 드래그 핸들 영역 가로 전체를 클릭 가능하게 wrap한다.
 * 사용자가 핸들 또는 그 주변을 톡 누르면 시트가 부드럽게 닫힌다 — 위로 스와이프 외에 빠른 dismiss 경로 제공.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClickableDragHandle(
    scope: CoroutineScope,
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    // ripple 없이 — 핸들 자체의 시각만 유지한다.
                    indication = null,
                ) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
    ) {
        BottomSheetDefaults.DragHandle()
    }
}

private data class CopyLabels(
    val zip: String,
    val road: String,
    val jibun: String,
    val english: String,
)

@Composable
private fun DetailSheetContent(
    address: Address,
    query: String,
    coordinatePhase: CoordinatePhase,
    lastCopied: CopyField?,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onRetry: () -> Unit,
    onCopy: (CopyField, String, String) -> Unit,
    labels: CopyLabels,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        DetailHeader(
            address = address,
            query = query,
            lastCopied = lastCopied,
            isFavorite = isFavorite,
            onFavoriteToggle = onFavoriteToggle,
        )
        MapArea(phase = coordinatePhase, onRetry = onRetry)
        MapDeepLinkButtons(
            coord = (coordinatePhase as? CoordinatePhase.Success)?.coordinate,
            address = address.roadAddress,
            modifier = Modifier.padding(horizontal = ZipkrSpacing.md),
        )
        CopyBar(
            onCopyZip = { onCopy(CopyField.Zip, labels.zip, address.zipCode) },
            onCopyRoad = { onCopy(CopyField.Road, labels.road, address.roadAddress) },
            onCopyJibun = { onCopy(CopyField.Jibun, labels.jibun, address.jibunAddress) },
            onCopyEnglish = { onCopy(CopyField.English, labels.english, address.englishAddress) },
        )
    }
}

@Composable
private fun MapArea(
    phase: CoordinatePhase,
    onRetry: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(MAP_HEIGHT)
                .padding(horizontal = ZipkrSpacing.md),
    ) {
        when (phase) {
            CoordinatePhase.Loading -> MapPlaceholder { CircularProgressIndicator() }
            is CoordinatePhase.Success ->
                KakaoMapWebView(
                    coord = phase.coordinate,
                    jsKey = BuildConfig.KAKAO_JS_KEY,
                    modifier = Modifier.fillMaxSize(),
                )
            is CoordinatePhase.Failure -> MapFailure(onRetry = onRetry)
        }
    }
}

@Composable
private fun MapFailure(onRetry: () -> Unit) {
    MapPlaceholder {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.map_load_failed),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.map_load_retry))
            }
        }
    }
}

@Composable
private fun MapPlaceholder(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(PLACEHOLDER_RADIUS),
                ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
