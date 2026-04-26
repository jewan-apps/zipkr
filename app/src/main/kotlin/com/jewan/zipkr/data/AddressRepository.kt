package com.jewan.zipkr.data

import com.jewan.zipkr.data.provider.Result

/**
 * 도메인 레포지토리 인터페이스이다.
 * UI/ViewModel은 이 계약만 알며, 내부 Provider 구현은 모른다.
 * v2에서 카카오 Provider 추가 시 본 인터페이스는 변경 없다.
 */
interface AddressRepository {
    suspend fun search(query: String): Result<List<Address>>
}
