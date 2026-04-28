package com.jewan.zipkr.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.components.CopyField
import com.jewan.zipkr.ui.theme.ZipkrSpacing

private val ZIP_JUMBO_SIZE = 32.sp
private val LABEL_BODY_SIZE = 13.sp
private val SUBTLE_BODY_SIZE = 12.sp
private const val HIGHLIGHT_TRANSITION_MS = 220
private const val HIGHLIGHT_BG_ALPHA = 0.18f
private const val HIGHLIGHT_SCALE = 1.04f

/**
 * 상세 시트 상단의 점보 우편번호 + 풀 주소 영역이다.
 * 건물명은 빈 문자열일 때 숨긴다.
 *
 * lastCopied가 set되면 해당 텍스트가 brand 톤으로 잠깐 highlight된다 — "어떤 내용이 클립보드에 들어갔는지" 인지.
 * 배경 brand-soft tint + 살짝 scale + 텍스트 색 brand 조합으로 강한 시각 강조.
 */
@Composable
fun DetailHeader(
    address: Address,
    lastCopied: CopyField? = null,
    modifier: Modifier = Modifier,
) {
    val brand = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val roadColor by highlightColor(lastCopied == CopyField.Road, brand, onSurface)
    val jibunColor by highlightColor(lastCopied == CopyField.Jibun, brand, onSurfaceVariant)
    val englishColor by highlightColor(lastCopied == CopyField.English, brand, onSurfaceVariant)
    val zipHighlighted = lastCopied == CopyField.Zip

    Column(
        modifier = modifier.padding(horizontal = ZipkrSpacing.md, vertical = ZipkrSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        DetailHeaderBody(
            address = address,
            lastCopied = lastCopied,
            brand = brand,
            roadColor = roadColor,
            jibunColor = jibunColor,
            englishColor = englishColor,
        )
    }
}

@Composable
private fun DetailHeaderBody(
    address: Address,
    lastCopied: CopyField?,
    brand: Color,
    roadColor: Color,
    jibunColor: Color,
    englishColor: Color,
) {
    val zipHighlighted = lastCopied == CopyField.Zip
    HighlightableLine(highlighted = zipHighlighted, brand = brand) {
        Text(
            text = address.zipCode,
            fontSize = ZIP_JUMBO_SIZE,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (zipHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = brand,
        )
    }
    HighlightableLine(highlighted = lastCopied == CopyField.Road, brand = brand) {
        Text(
            text = address.roadAddress,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = roadColor,
        )
    }
    HighlightableLine(highlighted = lastCopied == CopyField.Jibun, brand = brand) {
        Text(
            text = address.jibunAddress,
            fontSize = LABEL_BODY_SIZE,
            color = jibunColor,
        )
    }
    if (address.buildingName.isNotEmpty()) {
        Text(
            text = address.buildingName,
            fontSize = LABEL_BODY_SIZE,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
    }
    HighlightableLine(highlighted = lastCopied == CopyField.English, brand = brand) {
        Text(
            text = address.englishAddress,
            fontSize = SUBTLE_BODY_SIZE,
            fontStyle = FontStyle.Italic,
            color = englishColor,
        )
    }
}

@Composable
private fun highlightColor(
    active: Boolean,
    highlightColor: Color,
    baseColor: Color,
) = animateColorAsState(
    targetValue = if (active) highlightColor else baseColor,
    animationSpec = tween(durationMillis = HIGHLIGHT_TRANSITION_MS),
    label = "detail-header-color",
)

/**
 * 한 텍스트 줄을 brand-soft 배경 + 살짝 scale로 wrapping한다.
 * AddressResultCard.HighlightableLine과 동일 패턴이지만 별도 파일이라 따로 둔다.
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
        label = "detail-highlight-bg",
    )
    val scale by animateFloatAsState(
        targetValue = if (highlighted) HIGHLIGHT_SCALE else 1f,
        animationSpec = tween(durationMillis = HIGHLIGHT_TRANSITION_MS),
        label = "detail-highlight-scale",
    )
    Box(
        modifier =
            Modifier
                .scale(scale)
                .background(bg, RoundedCornerShape(8.dp))
                .padding(horizontal = ZipkrSpacing.xs, vertical = 2.dp),
    ) {
        content()
    }
}
