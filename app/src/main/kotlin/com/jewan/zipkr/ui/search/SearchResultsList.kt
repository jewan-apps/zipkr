package com.jewan.zipkr.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.components.AddressResultCard
import com.jewan.zipkr.ui.components.CopyField
import com.jewan.zipkr.ui.theme.ZipkrSpacing

// 리스트 끝에서 이 숫자만큼 앞서 다음 page를 미리 fetch해 UX를 부드럽게 한다.
private const val PREFETCH_THRESHOLD = 5

/**
 * 검색 결과 리스트이다.
 * 무한 스크롤: 마지막 아이템에서 PREFETCH_THRESHOLD번째 전 시점에 onLoadMore를 호출한다.
 * isLoadingMore 상태에서는 하단에 작은 스피너를 추가한다.
 */
@Composable
internal fun SearchResultsList(
    phase: SearchUiState.Phase.Success,
    query: String,
    onCopyAddress: (field: CopyField, label: String, text: String) -> Unit,
    onCardClick: (Address) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        items(phase.results) { address ->
            AddressResultCard(
                address = address,
                query = query,
                onCardClick = { onCardClick(address) },
                onCopyAddress = onCopyAddress,
            )
        }
        if (phase.isLoadingMore) {
            item { LoadMoreSpinner() }
        } else if (phase.loadMoreError != null) {
            item { LoadMoreRetry(onClick = onLoadMore) }
        }
    }

    AutoLoadMoreEffect(listState = listState, phase = phase, onLoadMore = onLoadMore)
}

@Composable
private fun LoadMoreSpinner() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(ZipkrSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun LoadMoreRetry(onClick: () -> Unit) {
    // page 2+ 실패는 누적 results를 유지한 채 마지막 행에만 작은 에러+재시도를 표시한다.
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(ZipkrSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.load_more_retry),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun AutoLoadMoreEffect(
    listState: LazyListState,
    phase: SearchUiState.Phase.Success,
    onLoadMore: () -> Unit,
) {
    // scroll 위치 변화만 derivedStateOf로 최적화한다. phase 조건은 LaunchedEffect 키로 정확히 처리해
    // page 2+ 진행/loadMoreError 발생 시 stale 캡처 없이 즉시 반영되게 한다.
    val endReached by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            total > 0 && lastVisible >= total - PREFETCH_THRESHOLD
        }
    }
    val readyForMore = phase.hasNext && !phase.isLoadingMore && phase.loadMoreError == null
    LaunchedEffect(endReached, readyForMore) {
        if (endReached && readyForMore) onLoadMore()
    }
}
