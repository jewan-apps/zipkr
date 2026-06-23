package com.jewan.zipkr.data

/**
 * 페이징 결과 wrapper이다.
 * items는 현재까지 로드된 누적 결과가 아니라 이번 page만 담는다 (누적은 ViewModel 책임).
 */
data class AddressPage(
    val items: List<Address>,
    val currentPage: Int,
    val totalCount: Int,
) {
    /** 다음 page가 더 있는지 (currentPage * pageSize < totalCount). */
    fun hasNext(pageSize: Int): Boolean = currentPage * pageSize < totalCount
}
