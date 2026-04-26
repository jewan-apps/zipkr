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
            val currentPage: Int,
            val hasNext: Boolean,
            val isLoadingMore: Boolean = false,
        ) : Phase

        data object Empty : Phase

        /**
         * 5자리 숫자(우편번호) 입력 시 API 대신 안내 화면으로 전환하는 Phase이다.
         * 행안부 API는 E0009로 거부하지만, 의도가 명확하므로 API 호출 없이 먼저 안내한다.
         */
        data object PostalCodeUnsupported : Phase

        /**
         * AppError 자체를 보존한다. 사용자 가시 문자열은 Composable 레이어에서
         * stringResource로 매핑해 ViewModel이 i18n에 무관하게 유지된다.
         */
        data class Error(
            val error: AppError,
        ) : Phase
    }
}
