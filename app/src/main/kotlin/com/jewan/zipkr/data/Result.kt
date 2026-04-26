package com.jewan.zipkr.data

/**
 * 도메인 에러를 포함한 결과 타입이다.
 * 도메인 계층(`data` 패키지 루트)에 위치해 Repository·Provider 계층이 모두 동일하게 의존한다.
 */
sealed class Result<out T> {
    data class Success<T>(
        val value: T,
    ) : Result<T>()

    data class Failure(
        val error: AppError,
    ) : Result<Nothing>()
}
