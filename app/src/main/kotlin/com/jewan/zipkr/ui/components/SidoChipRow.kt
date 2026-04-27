package com.jewan.zipkr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Sido
import com.jewan.zipkr.ui.theme.ZipkrSpacing

/**
 * 시·도 선택 칩 row이다.
 * "전체" + 17개 시·도, LazyRow 가로 스크롤. 순수 radio 그룹이며 toggle 동작은 없다.
 *
 * 같은 칩 재클릭은 ViewModel에서 noop으로 흡수된다 — 이중 검색·깜빡임 방지.
 * 선택 해제(전국으로 되돌리기)는 "전체" 칩을 통해서만 한다 (의도가 명확).
 */
@Composable
fun SidoChipRow(
    selected: Sido?,
    onSelect: (Sido?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
        contentPadding = PaddingValues(horizontal = ZipkrSpacing.xs),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.sido_chip_all)) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
        items(items = Sido.ORDERED, key = { it.name }) { sido ->
            FilterChip(
                selected = selected == sido,
                onClick = { onSelect(sido) },
                label = { Text(sido.displayName) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}
