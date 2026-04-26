package com.jewan.zipkr.data.provider

import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.Result

/**
 * 외부 데이터 소스의 일반화 인터페이스이다.
 * 행안부·카카오·자체 캐시 등 어떤 구현체든 이 계약만 따르면 교체 가능하다.
 */
interface AddressProvider {
    suspend fun search(query: String): Result<List<Address>>
}
