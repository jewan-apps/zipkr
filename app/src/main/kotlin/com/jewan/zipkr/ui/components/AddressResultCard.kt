package com.jewan.zipkr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * 검색 결과 단일 카드 (Unified Copy Bar 디자인 — frontend-design 스킬 OPTION B).
 *
 * 구조:
 * 1. CardBody: 도로명·지번·우편번호 점보(monospace, brand red)·영문주소
 * 2. HorizontalDivider
 * 3. CopyBar: 4-segment 액션 — 영문 / 지번 / 도로명(brand tint) / 우편번호(filled primary)
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
