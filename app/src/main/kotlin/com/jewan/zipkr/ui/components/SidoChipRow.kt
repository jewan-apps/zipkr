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
 * "전체" + 17개 시·도, LazyRow 가로 스크롤. 단일 선택(radio 동작)이며 같은 칩을 다시 누르면 해제(null)된다.
 *
 * 첫 칩 "전체"는 selectedSido == null과 매핑되고 클릭 시 항상 null을 호출한다 (toggle 아님 — radio 그룹의 default).
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
                onClick = { onSelect(if (selected == sido) null else sido) },
                label = { Text(sido.displayName) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}
