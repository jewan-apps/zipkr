package com.jewan.zipkr.ui.search

import com.jewan.zipkr.data.Address

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

        data class Error(
            val message: String,
        ) : Phase
    }
}
