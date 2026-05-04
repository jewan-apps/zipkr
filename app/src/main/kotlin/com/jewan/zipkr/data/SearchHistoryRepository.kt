package com.jewan.zipkr.data

import kotlinx.coroutines.flow.Flow

/**
 * 검색 히스토리(최근 본 주소)와 즐겨찾기 영속 인터페이스이다.
 *
 * - "최근 본 주소"는 사용자가 결과 카드 탭으로 detail sheet를 열 때 자동 누적된다 (저장 max RECENT_LIMIT).
 * - "즐겨찾기"는 detail sheet의 ★ 토글로 명시적으로 추가한다 (저장 무제한).
 *
 * UI 계층은 두 Flow를 collect만 하면 되고, 추가/삭제/토글은 suspend 함수로 호출한다.
 * Flow는 내부 변경 시 즉시 새 List를 emit한다 (DataStore Preferences 기반).
 */
interface SearchHistoryRepository {
    /** 최근 본 주소 — 가장 최근이 first, 최대 [RECENT_LIMIT]개. */
    fun observeRecent(): Flow<List<Address>>

    /** 즐겨찾기 — 추가 순서가 first. */
    fun observeFavorites(): Flow<List<Address>>

    /** 결과 카드 탭 → detail sheet 진입 시 호출. 동일 stableKey가 있으면 맨 앞으로 끌어올린다. */
    suspend fun addRecent(address: Address)

    /** 즐겨찾기 토글. 이미 즐겨찾기면 제거, 없으면 추가한다. */
    suspend fun toggleFavorite(address: Address)

    /** 칩 long-press 등으로 개별 삭제. */
    suspend fun removeRecent(address: Address)

    suspend fun removeFavorite(address: Address)

    companion object {
        /** 최근 본 주소 최대 개수 — 칩 행이 너무 길어지지 않도록 제한한다. */
        const val RECENT_LIMIT = 5
    }
}
