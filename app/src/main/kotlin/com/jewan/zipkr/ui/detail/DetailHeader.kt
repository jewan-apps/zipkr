package com.jewan.zipkr.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.components.CopyField
import com.jewan.zipkr.ui.theme.ZipkrMono
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.highlightQuery

private val ZIP_JUMBO_SIZE = 36.sp
private val ZIP_JUMBO_TRACKING = 2.sp
private val LABEL_BODY_SIZE = 13.sp
private val SUBTLE_BODY_SIZE = 12.sp
private const val HIGHLIGHT_TRANSITION_MS = 220
private const val HIGHLIGHT_BG_ALPHA = 0.18f
private const val HIGHLIGHT_SCALE = 1.04f

// 즐겨찾기 ★ 토글 시각 토큰 — 우편번호 점보 옆에 작게 부착해 미니멀 톤을 유지한다.
private val FAVORITE_TAP_SIZE = 40.dp
private val FAVORITE_ICON_SIZE = 24.dp

/**
 * 상세 시트 상단의 점보 우편번호 + 풀 주소 영역이다.
 * 건물명은 jibunAddress 끝에 이미 포함돼 들어와서 별도 라인은 두지 않는다 (중복 노출 방지).
 *
 * lastCopied가 set되면 해당 텍스트가 brand 톤으로 잠깐 highlight된다.
 * query가 비어있지 않으면 도로명·지번·영문 주소 안의 매칭 토큰이 brand 톤으로 강조된다 (검색어 하이라이트).
 * v1.1k: isFavorite + onFavoriteToggle이 주어지면 우편번호 점보 우측에 ★ 토글이 노출된다.
 */
@Composable
fun DetailHeader(
    address: Address,
    query: String = "",
    lastCopied: CopyField? = null,
    isFavorite: Boolean = false,
    onFavoriteToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val brand = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val roadColor by highlightColor(lastCopied == CopyField.Road, brand, onSurface)
    val jibunColor by highlightColor(lastCopied == CopyField.Jibun, brand, onSurfaceVariant)
    val englishColor by highlightColor(lastCopied == CopyField.English, brand, onSurfaceVariant)

    Column(
        modifier = modifier.padding(horizontal = ZipkrSpacing.md, vertical = ZipkrSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        DetailHeaderBody(
            address = address,
            query = query,
            lastCopied = lastCopied,
            brand = brand,
            roadColor = roadColor,
            jibunColor = jibunColor,
            englishColor = englishColor,
            isFavorite = isFavorite,
            onFavoriteToggle = onFavoriteToggle,
        )
    }
}

@Composable
private fun DetailHeaderBody(
    address: Address,
    query: String,
    lastCopied: CopyField?,
    brand: Color,
    roadColor: Color,
    jibunColor: Color,
    englishColor: Color,
    isFavorite: Boolean,
    onFavoriteToggle: (() -> Unit)?,
) {
    val highlightStyle = SpanStyle(color = brand, fontWeight = FontWeight.SemiBold)
    ZipRow(
        zipCode = address.zipCode,
        zipHighlighted = lastCopied == CopyField.Zip,
        brand = brand,
        isFavorite = isFavorite,
        onFavoriteToggle = onFavoriteToggle,
    )
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
            fontSize = LABEL_BODY_SIZE,
            color = jibunColor,
        )
    }
    HighlightableLine(highlighted = lastCopied == CopyField.English, brand = brand) {
        // 영어 검색은 아직 지원하지 않으므로(행안부 API가 한글 매칭 전용) 영어 주소엔 highlight를 적용하지 않는다.
        Text(
            text = address.englishAddress,
            fontSize = SUBTLE_BODY_SIZE,
            fontStyle = FontStyle.Italic,
            color = englishColor,
        )
    }
}

/**
 * 점보 우편번호 + 우측 ★ 토글 한 줄이다. ★는 onFavoriteToggle이 주어진 경우에만 노출한다.
 */
@Composable
private fun ZipRow(
    zipCode: String,
    zipHighlighted: Boolean,
    brand: Color,
    isFavorite: Boolean,
    onFavoriteToggle: (() -> Unit)?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            HighlightableLine(highlighted = zipHighlighted, brand = brand) {
                Text(
                    text = zipCode,
                    fontSize = ZIP_JUMBO_SIZE,
                    fontFamily = ZipkrMono,
                    fontWeight = if (zipHighlighted) FontWeight.ExtraBold else FontWeight.Bold,
                    letterSpacing = ZIP_JUMBO_TRACKING,
                    color = brand,
                )
            }
        }
        if (onFavoriteToggle != null) {
            FavoriteToggle(isFavorite = isFavorite, onToggle = onFavoriteToggle, brand = brand)
        }
    }
}

@Composable
private fun FavoriteToggle(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    brand: Color,
) {
    val descriptionRes = if (isFavorite) R.string.favorite_remove else R.string.favorite_add
    IconButton(onClick = onToggle, modifier = Modifier.size(FAVORITE_TAP_SIZE)) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(descriptionRes),
            tint = if (isFavorite) brand else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(FAVORITE_ICON_SIZE),
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
