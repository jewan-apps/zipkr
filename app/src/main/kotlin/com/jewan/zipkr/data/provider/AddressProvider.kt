package com.jewan.zipkr.data.provider

import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AppError

/**
 * 외부 데이터 소스의 일반화 인터페이스이다.
 * 행안부·카카오·자체 캐시 등 어떤 구현체든 이 계약만 따르면 교체 가능하다.
 */
interface AddressProvider {
    suspend fun search(query: String): Result<List<Address>>
}

/**
 * 도메인 에러를 포함한 결과 타입이다.
 */
sealed class Result<out T> {
    data class Success<T>(
        val value: T,
    ) : Result<T>()

    data class Failure(
        val error: AppError,
    ) : Result<Nothing>()
}
