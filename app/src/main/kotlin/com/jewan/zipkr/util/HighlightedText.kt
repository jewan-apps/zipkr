package com.jewan.zipkr.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

private val WHITESPACE_REGEX = Regex("\\s+")

/**
 * 텍스트에서 query 토큰과 매칭되는 모든 substring에 SpanStyle을 적용한 AnnotatedString을 반환한다.
 *
 * - query는 공백으로 split하여 각 토큰별로 case-insensitive substring 매칭한다.
 * - 한 텍스트 안에서 같은 토큰이 여러 번 나오면 모두 강조한다 (사용자가 "안중" 검색 시 "안중읍 안중리" 둘 다).
 * - 빈 query 또는 빈 토큰만 있으면 plain AnnotatedString 반환 (no-op).
 * - 매칭이 겹치면 Compose가 SpanStyle을 누적 적용하므로 별도 처리 없이 안전하다.
 *
 * 시·도 prefix("서울특별시")는 ViewModel이 합성한 거지 사용자 입력이 아니므로 호출자가 raw query만 넘겨야 한다.
 */
fun highlightQuery(
    text: String,
    query: String,
    style: SpanStyle,
): AnnotatedString {
    val tokens = query.trim().split(WHITESPACE_REGEX).filter { it.isNotBlank() }
    if (tokens.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        tokens.forEach { token ->
            var startIndex = 0
            while (startIndex < text.length) {
                val foundIndex = text.indexOf(token, startIndex, ignoreCase = true)
                if (foundIndex == -1) break
                addStyle(style = style, start = foundIndex, end = foundIndex + token.length)
                startIndex = foundIndex + token.length
            }
        }
    }
}
