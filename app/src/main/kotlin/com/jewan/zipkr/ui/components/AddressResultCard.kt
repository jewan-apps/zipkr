package com.jewan.zipkr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay

// CardBody 우편번호 점보 + label 시각 토큰
private val ZIP_NUMBER_SIZE = 28.sp
private val ZIP_LABEL_SIZE = 10.sp
private val ZIP_LABEL_TRACKING = 1.5.sp
private val ZIP_LABEL_BOTTOM_PAD = 4.dp

// 본문 텍스트 highlight 시각 토큰 — CopyBar segment 누르면 해당 본문 줄이 brand 톤으로 잠깐 강조된다.
// 배경 tint + 텍스트 색 + scale 조합으로 강한 시각 인지를 만든다.
private const val HIGHLIGHT_DURATION_MS = 1200L
private const val HIGHLIGHT_TRANSITION_MS = 220
private const val HIGHLIGHT_BG_ALPHA = 0.18f
private const val HIGHLIGHT_SCALE = 1.04f

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

    // 사용자가 방금 복사한 항목 — 본문 텍스트가 brand 색으로 잠깐 highlight된다.
    var lastCopied by remember { mutableStateOf<CopyField?>(null) }
    LaunchedEffect(lastCopied) {
        if (lastCopied != null) {
            delay(HIGHLIGHT_DURATION_MS)
            lastCopied = null
        }
    }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            CardBody(address = address, query = query, lastCopied = lastCopied)
            HorizontalDivider()
            CopyBar(
                onCopyZip = {
                    onCopyAddress(zipLabel, address.zipCode)
                    lastCopied = CopyField.Zip
                },
                onCopyRoad = {
                    onCopyAddress(roadLabel, address.roadAddress)
                    lastCopied = CopyField.Road
                },
                onCopyJibun = {
                    onCopyAddress(jibunLabel, address.jibunAddress)
                    lastCopied = CopyField.Jibun
                },
                onCopyEnglish = {
                    onCopyAddress(englishLabel, address.englishAddress)
                    lastCopied = CopyField.English
                },
            )
        }
    }
}

@Composable
private fun CardBody(
    address: Address,
    query: String,
    lastCopied: CopyField?,
) {
    val highlightStyle =
        SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    val brand = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val roadColor by animatedHighlightColor(lastCopied == CopyField.Road, brand, onSurface)
    val jibunColor by animatedHighlightColor(lastCopied == CopyField.Jibun, brand, onSurfaceVariant)
    val englishColor by animatedHighlightColor(lastCopied == CopyField.English, brand, onSurfaceVariant)

    Column(
        modifier = Modifier.padding(ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        HighlightableLine(highlighted = lastCopied == CopyField.Road, brand = brand) {
            Text(
                text = highlightQuery(address.roadAddress, query, highlightStyle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = roadColor,
            )
        }
        HighlightableLine(highlighted = lastCopied == CopyField.Jibun, brand = brand) {
            Text(
                text = highlightQuery(address.jibunAddress, query, highlightStyle),
                style = MaterialTheme.typography.bodySmall,
                color = jibunColor,
            )
        }
        HighlightableLine(highlighted = lastCopied == CopyField.Zip, brand = brand) {
            ZipJumboRow(
                zipCode = address.zipCode,
                highlighted = lastCopied == CopyField.Zip,
                brand = brand,
                onSurfaceVariant = onSurfaceVariant,
            )
        }
        HighlightableLine(highlighted = lastCopied == CopyField.English, brand = brand) {
            Text(
                text = highlightQuery(address.englishAddress, query, highlightStyle),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = englishColor,
            )
        }
    }
}

/**
 * 한 텍스트 줄을 brand-soft 배경 + 살짝 scale로 wrapping한다.
 * highlighted=true 시 배경이 brand-soft alpha로 잠깐 들어갔다가 transparent로 사라지고,
 * scale 1.0 → HIGHLIGHT_SCALE → 1.0으로 부드러운 확대-원복.
 */
@Composable
private fun HighlightableLine(
    highlighted: Boolean,
    brand: Color,
    content: @Composable () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (highlighted) brand.copy(alpha = HIGHLIGHT_BG_ALPHA) else Color.Transparent,
        animationSpec = tween(durationMillis = HIGHLIGHT_TRANSITION_MS),
        label = "highlight-bg",
    )
    val scale by animateFloatAsState(
        targetValue = if (highlighted) HIGHLIGHT_SCALE else 1f,
        animationSpec = tween(durationMillis = HIGHLIGHT_TRANSITION_MS),
        label = "highlight-scale",
    )
    androidx.compose.foundation.layout.Box(
        modifier =
            Modifier
                .scale(scale)
                .background(bg, RoundedCornerShape(8.dp))
                .padding(horizontal = ZipkrSpacing.xs, vertical = 2.dp),
    ) {
        content()
    }
}

@Composable
private fun ZipJumboRow(
    zipCode: String,
    highlighted: Boolean,
    brand: Color,
    onSurfaceVariant: Color,
) {
    Row(
        modifier = Modifier.padding(top = ZipkrSpacing.xs),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        Text(
            text = zipCode,
            fontSize = ZIP_NUMBER_SIZE,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (highlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = brand,
        )
        Text(
            text = stringResource(R.string.copy_zip_label),
            fontSize = ZIP_LABEL_SIZE,
            letterSpacing = ZIP_LABEL_TRACKING,
            fontWeight = FontWeight.SemiBold,
            color = if (highlighted) brand else onSurfaceVariant,
            modifier = Modifier.padding(bottom = ZIP_LABEL_BOTTOM_PAD),
        )
    }
}

@Composable
private fun animatedHighlightColor(
    highlighted: Boolean,
    highlightColor: Color,
    baseColor: Color,
) = animateColorAsState(
    targetValue = if (highlighted) highlightColor else baseColor,
    animationSpec = tween(durationMillis = HIGHLIGHT_TRANSITION_MS),
    label = "card-body-highlight",
)

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
