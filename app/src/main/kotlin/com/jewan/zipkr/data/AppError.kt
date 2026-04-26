package com.jewan.zipkr.data

/**
 * 도메인 에러 타입이다.
 * 사용자 응답에 내부 예외 원문은 노출하지 않으며 (헌법 §1.9),
 * UI는 type만 보고 친화 메시지를 매핑한다.
 */
sealed class AppError(open val cause: Throwable? = null) {
    data class Network(override val cause: Throwable? = null) : AppError(cause)
    data class ApiBadResponse(val code: String, val message: String) : AppError()
    data class Unknown(override val cause: Throwable? = null) : AppError(cause)
}
