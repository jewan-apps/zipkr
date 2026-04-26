package com.jewan.zipkr.ui.search

import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AppError

/**
 * 검색 화면의 단일 진실 상태이다.
 */
data class SearchUiState(
    val query: String = "",
    val phase: Phase = Phase.Idle,
) {
    sealed interface Phase {
        data object Idle : Phase

        data object Loading : Phase

        data class Success(
            val results: List<Address>,
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
