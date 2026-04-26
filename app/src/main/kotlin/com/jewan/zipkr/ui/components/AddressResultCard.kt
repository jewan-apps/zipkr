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
import androidx.compose.material3.Card
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

// CardBody 우편번호 점보 + label 시각 토큰
private val ZIP_NUMBER_SIZE = 28.sp
private val ZIP_LABEL_SIZE = 10.sp
private val ZIP_LABEL_TRACKING = 1.5.sp
private val ZIP_LABEL_BOTTOM_PAD = 4.dp

// CopyBar segment 시각 토큰 (4 segment: 우편 1.3 · 도로명 1 · 지번 1 · 영문 1)
private const val ZIP_SEGMENT_WEIGHT = 1.3f
private const val SECONDARY_SEGMENT_WEIGHT = 1f
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
            CardBody(address)
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
private fun CardBody(address: Address) {
    Column(
        modifier = Modifier.padding(ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        Text(
            text = address.roadAddress,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = address.jibunAddress,
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
            text = address.englishAddress,
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
        // Primary segment — filled brand. 시각 위계의 주인공.
        Button(
            onClick = onCopyZip,
            modifier =
                Modifier
                    .weight(ZIP_SEGMENT_WEIGHT)
                    .fillMaxHeight(),
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
        SegmentDivider()
        SecondarySegment(
            label = stringResource(R.string.copy_road_short),
            onClick = onCopyRoad,
            modifier = Modifier.weight(SECONDARY_SEGMENT_WEIGHT),
        )
        SegmentDivider()
        SecondarySegment(
            label = stringResource(R.string.copy_jibun_short),
            onClick = onCopyJibun,
            modifier = Modifier.weight(SECONDARY_SEGMENT_WEIGHT),
        )
        SegmentDivider()
        SecondarySegment(
            label = stringResource(R.string.copy_english_short),
            onClick = onCopyEnglish,
            modifier = Modifier.weight(SECONDARY_SEGMENT_WEIGHT),
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
                ),
            onCardClick = {},
            onCopyAddress = { _, _ -> },
        )
    }
}
