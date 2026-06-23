package com.jewan.zipkr.ui.search

import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Sido

/**
 * 검색 화면의 단일 진실 상태이다.
 * selectedSido는 시·도 칩 선택 상태이고 null은 "전체"이다.
 * 사용자가 입력한 query는 그대로 유지되고, ViewModel이 effective query에 prefix를 합성해 호출한다.
 */
data class SearchUiState(
    val query: String = "",
    val selectedSido: Sido? = null,
    val phase: Phase = Phase.Idle,
) {
    sealed interface Phase {
        data object Idle : Phase

        data object Loading : Phase

        data class Success(
            val results: List<Address>,
            val currentPage: Int,
            val hasNext: Boolean,
            val isLoadingMore: Boolean = false,
            // page 2+ fetch 실패 시 누적 results는 유지하고 본 필드만 채운다.
            // 사용자가 보던 결과를 잃지 않으면서 마지막 행에 작은 에러+재시도를 띄울 수 있다.
            val loadMoreError: AppError? = null,
        ) : Phase

        data object Empty : Phase

        /**
         * AppError 자체를 보존한다. 사용자 가시 문자열은 Composable 레이어에서
         * stringResource로 매핑해 ViewModel이 i18n에 무관하게 유지된다.
         */
        data class Error(
            val error: AppError,
        ) : Phase
    }
}
