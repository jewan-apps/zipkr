package com.jewan.zipkr.ui.detail

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coordinatePhase by viewModel.coordinate.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val toastTpl = stringResource(R.string.copy_toast)

    val zipLabel = stringResource(R.string.copy_zip_label)
    val roadLabel = stringResource(R.string.copy_road_label)
    val jibunLabel = stringResource(R.string.copy_jibun_label)
    val englishLabel = stringResource(R.string.copy_english_label)

    LaunchedEffect(address.roadAddress) {
        viewModel.fetchCoordinate(address.roadAddress)
    }

    val onCopy: (String, String) -> Unit = { label, text ->
        context.copyToClipboard(label, text)
        view.lightHaptic()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, toastTpl.format(text), Toast.LENGTH_SHORT).show()
        }
    }

    // 사용자가 방금 복사한 항목 — DetailHeader 본문 텍스트가 brand 색으로 잠깐 highlight된다.
    var lastCopied by remember { mutableStateOf<CopyField?>(null) }
    LaunchedEffect(lastCopied) {
        if (lastCopied != null) {
            kotlinx.coroutines.delay(HIGHLIGHT_DURATION_MS)
            lastCopied = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = SHEET_RADIUS, topEnd = SHEET_RADIUS),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        DetailSheetContent(
            address = address,
            coordinatePhase = coordinatePhase,
            lastCopied = lastCopied,
            onRetry = { viewModel.fetchCoordinate(address.roadAddress) },
            onCopy = { field, label, text ->
                onCopy(label, text)
                lastCopied = field
            },
            labels = CopyLabels(zipLabel, roadLabel, jibunLabel, englishLabel),
        )
    }
}

private const val HIGHLIGHT_DURATION_MS = 800L

private data class CopyLabels(
    val zip: String,
    val road: String,
    val jibun: String,
    val english: String,
)

@Composable
private fun DetailSheetContent(
    address: Address,
    coordinatePhase: CoordinatePhase,
    lastCopied: CopyField?,
    onRetry: () -> Unit,
    onCopy: (CopyField, String, String) -> Unit,
    labels: CopyLabels,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        DetailHeader(address = address, lastCopied = lastCopied)
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
