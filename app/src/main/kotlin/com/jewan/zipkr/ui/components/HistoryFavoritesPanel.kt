package com.jewan.zipkr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.theme.ZipkrMono
import com.jewan.zipkr.ui.theme.ZipkrSpacing

private val SECTION_TITLE_SIZE = 12.sp
private val SECTION_TITLE_TRACKING = 1.2.sp
private val ROW_VPAD = 10.dp
private val ZIP_FONT_SIZE = 14.sp
private const val DIVIDER_ALPHA = 0.4f

/**
 * 검색바 비어있는 Idle 상태에서 EmptyState 자리에 노출되는 즐겨찾기·최근 본 주소 패널이다.
 * UI 영역을 새로 추가하지 않고 빈 자리만 재활용해 미니멀 톤을 유지한다.
 *
 * 항목 탭 → onAddressClick으로 detail sheet를 즉시 연다 (재검색 없이 바로 진입).
 */
@Composable
fun HistoryFavoritesPanel(
    favorites: List<Address>,
    recent: List<Address>,
    onAddressClick: (Address) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        if (favorites.isNotEmpty()) {
            item {
                SectionTitle(
                    text = stringResource(R.string.favorites_section_title),
                    icon = Icons.Filled.Star,
                    iconTint = MaterialTheme.colorScheme.primary,
                )
            }
            items(favorites, key = { "fav-${it.stableKey}" }) { addr ->
                AddressRow(address = addr, onClick = { onAddressClick(addr) })
            }
        }
        if (recent.isNotEmpty()) {
            item {
                SectionTitle(
                    text = stringResource(R.string.recent_section_title),
                    icon = Icons.Outlined.Schedule,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(recent, key = { "rec-${it.stableKey}" }) { addr ->
                AddressRow(address = addr, onClick = { onAddressClick(addr) })
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.padding(top = ZipkrSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.padding(end = 2.dp),
        )
        Text(
            text = text,
            fontSize = SECTION_TITLE_SIZE,
            letterSpacing = SECTION_TITLE_TRACKING,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AddressRow(
    address: Address,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = ROW_VPAD, horizontal = ZipkrSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
        ) {
            // 도로명주소 우선 — 사용자가 인지하는 핵심 정보. 한 줄 truncate.
            Text(
                text = address.roadAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = address.zipCode,
                fontFamily = ZipkrMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = ZIP_FONT_SIZE,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = DIVIDER_ALPHA))
    }
}
