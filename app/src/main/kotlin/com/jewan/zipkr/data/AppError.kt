package com.jewan.zipkr.data

/**
 * 도메인 에러 타입이다.
 * 사용자 응답에 내부 예외 원문은 노출하지 않으며 (헌법 §1.9),
 * UI는 type만 보고 친화 메시지를 매핑한다.
 *
 * Network/Unknown은 Throwable cause를 갖지만 data class로 두면 `equals()`가
 * Throwable identity 비교를 하게 되어 같은 메시지의 IOException 두 개가 다르게 취급된다.
 * 따라서 일반 class로 선언하고 cause는 디버깅 보조 필드로만 다룬다.
 */
sealed class AppError {
    abstract val cause: Throwable?

    class Network(
        override val cause: Throwable? = null,
    ) : AppError() {
        override fun toString(): String = "AppError.Network(cause=$cause)"
    }

    /**
     * 외부 API가 정상 HTTP로 응답했지만 도메인 코드가 실패인 경우이다.
     *
     * code 분류 상수는 UI/재시도 전략 분기를 위해 노출한다.
     * 외부 API 원문 메시지는 본 타입에 담지 않는다 — UI에서 실수로 노출되는 경로 차단(헌법 §1.9).
     * 디버깅용 원문은 Provider 단의 Timber 로그에서만 확인한다.
     */
    data class ApiBadResponse(
        val code: String,
    ) : AppError() {
        override val cause: Throwable? = null

        // 행안부 API 에러 코드별 의미 헬퍼이다.
        val isAuthError: Boolean get() = code in AUTH_ERROR_CODES // E0001/E0002/E0014
        val isPathError: Boolean get() = code == PATH_ERROR_CODE // E0003
        val isEmpty: Boolean get() = code == EMPTY_QUERY_CODE // E0005
        val isQueryTooBroad: Boolean get() = code == QUERY_TOO_BROAD_CODE // E0006 (광역만 입력 등)
        val isQueryTooShort: Boolean get() = code == QUERY_TOO_SHORT_CODE // E0008 (한 글자 미만)
        val isNumericOnly: Boolean get() = code == NUMERIC_ONLY_CODE // E0009 (숫자만 입력)
        val isInvalidQuery: Boolean get() = code in INVALID_QUERY_CODES // E0010/E0013

        companion object {
            // 행안부 도로명주소 API errorCode 명세이다.
            // https://business.juso.go.kr/jst/jstRoadNmAddrApiSearch
            val AUTH_ERROR_CODES: Set<String> = setOf("E0001", "E0002", "E0014")
            const val PATH_ERROR_CODE = "E0003"
            const val EMPTY_QUERY_CODE = "E0005"
            const val QUERY_TOO_BROAD_CODE = "E0006"
            const val QUERY_TOO_SHORT_CODE = "E0008"
            const val NUMERIC_ONLY_CODE = "E0009"
            val INVALID_QUERY_CODES: Set<String> = setOf("E0010", "E0013")
        }
    }

    class Unknown(
        override val cause: Throwable? = null,
    ) : AppError() {
        override fun toString(): String = "AppError.Unknown(cause=$cause)"
    }
}
