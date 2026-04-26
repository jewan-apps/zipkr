package com.jewan.zipkr.data

import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.Result
import javax.inject.Inject

/**
 * 도메인 레포지토리 구현체이다.
 * MVP 단계는 단일 Provider 위임. v2에서 다중 Provider 통합 전략 추가 가능.
 */
class AddressRepositoryImpl
    @Inject
    constructor(
        private val provider: AddressProvider,
    ) : AddressRepository {
        override suspend fun search(query: String): Result<List<Address>> = provider.search(query)
    }
