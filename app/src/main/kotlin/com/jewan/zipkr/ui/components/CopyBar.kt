package com.jewan.zipkr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R

// CopyBar segment 시각 토큰 — 듀얼 강조 (우편 1.4 · 도로명 1.4 · 지번 1 · 영문 1).
// 우편번호와 도로명주소가 양대 use case라 둘 다 시각 위계 위에 둔다.
private const val ZIP_SEGMENT_WEIGHT = 1.4f
private const val ROAD_SEGMENT_WEIGHT = 1.4f
private const val SECONDARY_SEGMENT_WEIGHT = 1f

// 도로명 segment의 brand tint 배경 alpha. 우편(filled)보다는 약하게, plain보다는 강하게.
private const val ROAD_TINT_ALPHA = 0.10f
private val SEGMENT_LABEL_SIZE = 12.sp
private val SEGMENT_VPAD = 14.dp
private val SEGMENT_DIVIDER_WIDTH = 1.dp

/**
 * Unified Copy Bar — 4-segment 액션 (영문 / 지번 / 도로명 brand tint / 우편번호 filled primary)이다.
 *
 * AddressResultCard와 DetailSheet에서 공통으로 사용한다.
 * 좌→우 순서: 보조 → 핵심 (모바일 thumb reach·확인 다이얼로그 표준 — primary가 우측).
 */
@Composable
internal fun CopyBar(
    onCopyZip: () -> Unit,
    onCopyRoad: () -> Unit,
    onCopyJibun: () -> Unit,
    onCopyEnglish: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
    ) {
        // 좌→우 순서: 보조 → 핵심 (모바일 thumb reach·확인 다이얼로그 표준 — primary가 우측).
        // 3단 plain — 보조 use case.
        SecondarySegment(
            label = stringResource(R.string.copy_english_short),
            onClick = onCopyEnglish,
            modifier = Modifier.weight(SECONDARY_SEGMENT_WEIGHT),
        )
        SegmentDivider()
        SecondarySegment(
            label = stringResource(R.string.copy_jibun_short),
            onClick = onCopyJibun,
            modifier = Modifier.weight(SECONDARY_SEGMENT_WEIGHT),
        )
        SegmentDivider()
        // 2단 brand tint — UC 2·3·4 (카톡 공유 / 공식 문서 / 택배).
        RoadSegment(
            onClick = onCopyRoad,
            modifier = Modifier.weight(ROAD_SEGMENT_WEIGHT),
        )
        // 1단 primary filled — UC 1·4 trigger. 도로명·우편 사이 divider 없음 — 톤 차별이 separator.
        ZipSegment(
            onClick = onCopyZip,
            modifier = Modifier.weight(ZIP_SEGMENT_WEIGHT),
        )
    }
}

@Composable
private fun ZipSegment(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        contentPadding = PaddingValues(vertical = SEGMENT_VPAD),
    ) {
        Text(
            text = stringResource(R.string.copy_zip_short),
            fontSize = SEGMENT_LABEL_SIZE,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RoadSegment(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        colors =
            ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = ROAD_TINT_ALPHA),
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        contentPadding = PaddingValues(vertical = SEGMENT_VPAD),
    ) {
        Text(
            text = stringResource(R.string.copy_road_short),
            fontSize = SEGMENT_LABEL_SIZE,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SecondarySegment(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        contentPadding = PaddingValues(vertical = SEGMENT_VPAD),
    ) {
        Text(
            text = label,
            fontSize = SEGMENT_LABEL_SIZE,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * Material3 VerticalDivider는 1.2.0+ 필요. 우리 BOM에 포함됐는지 불확실하므로
 * 동등한 효과의 단순 Box로 대체한다 (1dp width × 부모 높이 fill, outline 색).
 */
@Composable
private fun SegmentDivider() {
    Box(
        modifier =
            Modifier
                .width(SEGMENT_DIVIDER_WIDTH)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
