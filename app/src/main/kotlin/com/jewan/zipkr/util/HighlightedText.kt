package com.jewan.zipkr.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

private val WHITESPACE_REGEX = Regex("\\s+")

/**
 * 텍스트에서 query 토큰과 매칭되는 모든 substring에 SpanStyle을 적용한 AnnotatedString을 반환한다.
 *
 * 매칭 전략:
 *  1) 공백 split 토큰별 case-insensitive substring 매칭 — "강남구 역삼동" 같은 분리된 키워드도 각각 강조한다.
 *  2) 인접 토큰 매칭 사이가 공백만이면 하나의 영역으로 merge — "안현로서7길 45"처럼 공백을 사이에 두고 붙어있는
 *     토큰들이 한 덩어리로 강조되도록 한다 (사이 공백도 영역에 포함된다).
 *  3) 1)에서 토큰이 한 개도 매칭되지 않았고 입력이 단일 토큰이면 query에서 공백을 제거한 normalized 형태로
 *     텍스트와 공백 무시 매칭한다. "안현로서7길45"처럼 붙여 입력해도 텍스트의 "안현로서7길 45"가 한 영역으로
 *     강조되도록 보장한다 — 따라서 "안현로서7길 45"와 "안현로서7길45"는 시각적으로 동일한 결과가 된다.
 *
 * - 한 텍스트 안에서 같은 토큰이 여러 번 나오면 모두 강조한다 (사용자가 "안중" 검색 시 "안중읍 안중리" 둘 다).
 * - 빈 query 또는 빈 토큰만 있으면 plain AnnotatedString 반환 (no-op).
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
        val matches = collectTokenMatches(text, tokens)
        if (matches.isNotEmpty()) {
            mergeAdjacentMatches(text, matches).forEach { (start, end) ->
                addStyle(style = style, start = start, end = end)
            }
        } else if (tokens.size == 1) {
            applyNormalizedMatches(text, tokens[0], style)
        }
    }
}

/**
 * 모든 토큰의 모든 매칭 위치를 (start, end exclusive) 리스트로 수집해 시작 인덱스 기준으로 정렬한다.
 */
private fun collectTokenMatches(
    text: String,
    tokens: List<String>,
): List<Pair<Int, Int>> = tokens.flatMap { matchAll(text, it) }.sortedBy { it.first }

private fun matchAll(
    text: String,
    token: String,
): List<Pair<Int, Int>> {
    val out = mutableListOf<Pair<Int, Int>>()
    var startIndex = 0
    while (startIndex < text.length) {
        val foundIndex = text.indexOf(token, startIndex, ignoreCase = true)
        if (foundIndex == -1) break
        out.add(foundIndex to (foundIndex + token.length))
        startIndex = foundIndex + token.length
    }
    return out
}

/**
 * 정렬된 매칭 리스트에서 인접 매칭 사이가 공백 문자만이면 한 영역으로 merge한다.
 * 그 외(글자가 끼어있거나 떨어진 위치)는 별개 span으로 유지한다.
 */
private fun mergeAdjacentMatches(
    text: String,
    sortedMatches: List<Pair<Int, Int>>,
): List<Pair<Int, Int>> {
    val merged = mutableListOf<Pair<Int, Int>>()
    sortedMatches.forEach { current ->
        val last = merged.lastOrNull()
        if (last != null && isWhitespaceGap(text, last, current)) {
            merged[merged.size - 1] = last.first to maxOf(last.second, current.second)
        } else {
            merged.add(current)
        }
    }
    return merged
}

private fun isWhitespaceGap(
    text: String,
    last: Pair<Int, Int>,
    current: Pair<Int, Int>,
): Boolean = current.first >= last.second && text.substring(last.second, current.first).isBlank()

/**
 * 공백을 무시하고 query를 텍스트와 char-by-char 매칭한다 (정규화 fallback).
 * 매칭이 잡히면 텍스트 상의 시작-끝 인덱스를 그대로 highlight (사이 공백도 영역에 포함된다).
 */
private fun AnnotatedString.Builder.applyNormalizedMatches(
    text: String,
    rawQuery: String,
    style: SpanStyle,
) {
    val normalizedQuery = rawQuery.replace(WHITESPACE_REGEX, "")
    if (normalizedQuery.isEmpty()) return
    var searchFrom = 0
    while (searchFrom < text.length) {
        val match = findNormalizedMatch(text, searchFrom, normalizedQuery) ?: break
        addStyle(style = style, start = match.first, end = match.second)
        searchFrom = match.second
    }
}

private fun findNormalizedMatch(
    text: String,
    startIdx: Int,
    normalizedQuery: String,
): Pair<Int, Int>? =
    (startIdx until text.length)
        .asSequence()
        .filter { !text[it].isWhitespace() }
        .mapNotNull { matchStart ->
            consumeMatch(text, matchStart, normalizedQuery)?.let { matchStart to it }
        }
        .firstOrNull()

/**
 * matchStart부터 normalizedQuery를 char-by-char 매칭한다 (텍스트의 공백은 skip).
 * 매칭 성공 시 끝 인덱스(exclusive)를, 실패 시 null을 반환한다.
 */
private fun consumeMatch(
    text: String,
    matchStart: Int,
    normalizedQuery: String,
): Int? {
    var qIdx = 0
    var tIdx = matchStart
    while (tIdx < text.length && qIdx < normalizedQuery.length) {
        val tc = text[tIdx]
        if (tc.isWhitespace()) {
            tIdx++
            continue
        }
        if (!tc.equals(normalizedQuery[qIdx], ignoreCase = true)) return null
        qIdx++
        tIdx++
    }
    return if (qIdx == normalizedQuery.length) tIdx else null
}
