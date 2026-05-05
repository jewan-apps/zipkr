package com.jewan.zipkr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Sido
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import kotlinx.coroutines.launch

// 시트 시각 토큰 — 차분한 minimal 톤
private val SHEET_HPAD = 20.dp
private val SHEET_TOP_PAD = 4.dp
private val SHEET_BOTTOM_PAD = 28.dp
private val GRID_GAP = 8.dp
private const val GRID_COLUMNS = 4
private val CHIP_HEIGHT = 44.dp
private val CHIP_RADIUS = 12.dp
private val CHIP_BORDER = 1.dp
// 4 column grid에서 영문 라벨(Gyeongbuk 등 9자)도 한 줄에 안전하게 들어가도록 12sp로 통일했다.
// 한글(2자)에선 살짝 작아 보이지만 calmer 톤에 맞고, 영문에선 칩 안에 깔끔히 들어간다.
private val CHIP_LABEL_SIZE = 12.sp
private val TITLE_SIZE = 16.sp
private val RESET_LABEL_SIZE = 13.sp

// 미선택 칩 배경 — 시트 베이스보다 살짝 elevated 한 톤
private const val INACTIVE_CHIP_TINT_ALPHA = 0.04f

/**
 * 시·도 선택 모달 바텀시트 (Pattern 4 — 헤더 우측 초기화 + swipe 닫기 + 17 시도 그리드).
 *
 * 액션 모델:
 * - 닫기: Material3 ModalBottomSheet의 swipe down + scrim tap (default)
 * - 초기화(전국으로): 헤더 우측 "↻ 초기화" 텍스트 — selected != null일 때만 표시
 * - 선택: 시도 칩 클릭 = 즉시 onSelect + dismiss (instant feedback)
 *
 * 그리드는 17개 시도만 노출. "전국"은 선택지가 아니라 "필터 없음" 상태이므로
 * 그리드 안에 칩으로 두지 않고 헤더 우측 초기화 액션으로 분리한다 (semantic 일치).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidoSelectorSheet(
    selected: Sido?,
    onSelect: (Sido?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val selectAndClose: (Sido?) -> Unit = { sido ->
        scope.launch {
            sheetState.hide()
            onSelect(sido)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = ZipkrSpacing.lg, topEnd = ZipkrSpacing.lg),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SHEET_HPAD)
                    .padding(top = SHEET_TOP_PAD, bottom = SHEET_BOTTOM_PAD),
        ) {
            SheetHeader(
                showReset = selected != null,
                onReset = { selectAndClose(null) },
            )
            SheetGrid(selected = selected, onSelect = selectAndClose)
        }
    }
}

@Composable
private fun SheetHeader(
    showReset: Boolean,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ZipkrSpacing.xs, vertical = ZipkrSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.sido_sheet_title),
            fontSize = TITLE_SIZE,
            fontWeight = FontWeight.SemiBold,
        )
        if (showReset) {
            Text(
                text = stringResource(R.string.sido_sheet_reset),
                fontSize = RESET_LABEL_SIZE,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .clickable(onClick = onReset)
                        .padding(horizontal = ZipkrSpacing.sm, vertical = ZipkrSpacing.xs),
            )
        }
    }
}

@Composable
private fun SheetGrid(
    selected: Sido?,
    onSelect: (Sido?) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        verticalArrangement = Arrangement.spacedBy(GRID_GAP),
        horizontalArrangement = Arrangement.spacedBy(GRID_GAP),
        contentPadding = PaddingValues(top = ZipkrSpacing.xs, bottom = ZipkrSpacing.xs),
    ) {
        items(items = Sido.ORDERED, key = { it.name }) { sido ->
            SidoCellChip(
                label = sido.label(),
                isActive = selected == sido,
                onClick = { onSelect(sido) },
            )
        }
    }
}

@Composable
private fun SidoCellChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val palette = chipPalette(isActive)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(CHIP_RADIUS),
        color = palette.container,
        contentColor = palette.content,
        border = palette.border,
        modifier = Modifier.height(CHIP_HEIGHT),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = ZipkrSpacing.xs),
        ) {
            Text(
                text = label,
                fontSize = CHIP_LABEL_SIZE,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = palette.content,
                // 영문 라벨이 한글(2자)보다 4~5배 길어 칩 너비를 초과하는 경우가 있어 1줄 강제 + 잘리면 ellipsis로 fallback.
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class ChipPalette(
    val container: Color,
    val content: Color,
    val border: BorderStroke?,
)

@Composable
private fun chipPalette(isActive: Boolean): ChipPalette =
    ChipPalette(
        container =
            if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = INACTIVE_CHIP_TINT_ALPHA)
            },
        content =
            if (isActive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        border =
            if (isActive) {
                null
            } else {
                BorderStroke(width = CHIP_BORDER, color = MaterialTheme.colorScheme.outline)
            },
    )
