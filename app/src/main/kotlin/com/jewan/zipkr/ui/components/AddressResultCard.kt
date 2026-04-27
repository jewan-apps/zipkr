package com.jewan.zipkr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.ui.theme.ZipkrTheme
import com.jewan.zipkr.util.highlightQuery

// CardBody 우편번호 점보 + label 시각 토큰
private val ZIP_NUMBER_SIZE = 28.sp
private val ZIP_LABEL_SIZE = 10.sp
private val ZIP_LABEL_TRACKING = 1.5.sp
private val ZIP_LABEL_BOTTOM_PAD = 4.dp

// CopyBar segment 시각 토큰 — 듀얼 강조 (우편 1.4 · 도로명 1.4 · 지번 1 · 영문 1).
// 우편번호와 도로명주소가 양대 use case라 둘 다 시각 위계 위에 둔다.
private const val ZIP_SEGMENT_WEIGHT = 1.4f
private const val ROAD_SEGMENT_WEIGHT = 1.4f
private const val SECONDARY_SEGMENT_WEIGHT = 1f

// 도로명 segment의 brand tint 배경 alpha. 우편(filled)보다는 약하게, plain보다는 강하게.
private const val ROAD_TINT_ALPHA = 0.10f
private val SEGMENT_LABEL_SIZE = 12.sp
private val SEGMENT_VPAD = 14.dp
private val SEGMENT_ICON_SIZE = 14.dp
private val SEGMENT_ICON_GAP = 6.dp
private val SEGMENT_DIVIDER_WIDTH = 1.dp

/**
 * 검색 결과 단일 카드 (Unified Copy Bar 디자인 — frontend-design 스킬 OPTION B).
 *
 * 구조:
 * 1. CardBody: 도로명·지번·우편번호 점보(monospace, brand red)·영문주소
 * 2. HorizontalDivider
 * 3. CopyBar: 3-segment 액션 — 우편번호(filled primary, weight 1.2) / 한글(plain, weight 1) / 영문(plain, weight 1)
 *
 * onCopyAddress(label, text)는 단일 콜백으로 통합한다 — 카드 내부에서 어떤 항목인지 구분해 호출한다.
 */
@Composable
fun AddressResultCard(
    address: Address,
    query: String,
    onCardClick: () -> Unit,
    onCopyAddress: (label: String, text: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zipLabel = stringResource(R.string.copy_zip_label)
    val roadLabel = stringResource(R.string.copy_road_label)
    val jibunLabel = stringResource(R.string.copy_jibun_label)
    val englishLabel = stringResource(R.string.copy_english_label)

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            CardBody(address = address, query = query)
            HorizontalDivider()
            CopyBar(
                onCopyZip = { onCopyAddress(zipLabel, address.zipCode) },
                onCopyRoad = { onCopyAddress(roadLabel, address.roadAddress) },
                onCopyJibun = { onCopyAddress(jibunLabel, address.jibunAddress) },
                onCopyEnglish = { onCopyAddress(englishLabel, address.englishAddress) },
            )
        }
    }
}

@Composable
private fun CardBody(
    address: Address,
    query: String,
) {
    // 매칭된 토큰만 brand 색 + SemiBold로 강조한다. ViewModel이 합성한 시·도 prefix는 query에 포함되지 않으므로
    // "서울특별시"가 빈번히 강조되는 노이즈는 발생하지 않는다.
    val highlightStyle =
        SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    Column(
        modifier = Modifier.padding(ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        Text(
            text = highlightQuery(address.roadAddress, query, highlightStyle),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = highlightQuery(address.jibunAddress, query, highlightStyle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // 우편번호 점보(brand red, monospace) + 작은 "우편번호" suffix label로 시각 anchor.
        Row(
            modifier = Modifier.padding(top = ZipkrSpacing.xs),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
        ) {
            Text(
                text = address.zipCode,
                fontSize = ZIP_NUMBER_SIZE,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.copy_zip_label),
                fontSize = ZIP_LABEL_SIZE,
                letterSpacing = ZIP_LABEL_TRACKING,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = ZIP_LABEL_BOTTOM_PAD),
            )
        }
        Text(
            text = highlightQuery(address.englishAddress, query, highlightStyle),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CopyBar(
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
        Icon(
            imageVector = Icons.Outlined.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(SEGMENT_ICON_SIZE),
        )
        Spacer(Modifier.width(SEGMENT_ICON_GAP))
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

@Preview(showBackground = true)
@Composable
private fun AddressResultCardPreview() {
    ZipkrTheme {
        AddressResultCard(
            address =
                Address(
                    zipCode = "06234",
                    roadAddress = "서울특별시 강남구 테헤란로 123",
                    jibunAddress = "역삼동 736-1",
                    englishAddress = "123, Teheran-ro, Gangnam-gu, Seoul",
                    buildingName = "",
                    sido = "",
                    sigungu = "",
                    eupmyeondong = "",
                ),
            query = "테헤란",
            onCardClick = {},
            onCopyAddress = { _, _ -> },
        )
    }
}
