package com.jewan.zipkr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
 * 시·도 선택 칩 그룹이다.
 * "전체" + 17개 시·도가 FlowRow로 2줄 wrap된다 — 가로 스크롤은 affordance가 약해
 * 우측에 가려진 지역(광주·울산·제주 등)을 사용자가 인지하지 못하는 문제가 있어 한눈에 다 보이게 한다.
 *
 * 순수 radio 그룹이며 toggle 동작은 없다 — 같은 칩 재클릭은 ViewModel에서 noop으로 흡수된다.
 * 선택 해제(전국으로 되돌리기)는 "전체" 칩을 통해서만 한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SidoChipRow(
    selected: Sido?,
    onSelect: (Sido?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.sido_chip_all)) },
            colors = FilterChipDefaults.filterChipColors(),
        )
        Sido.ORDERED.forEach { sido ->
            FilterChip(
                selected = selected == sido,
                onClick = { onSelect(sido) },
                label = { Text(sido.displayName) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}
