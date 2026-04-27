package com.jewan.zipkr.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HighlightedTextTest {
    private val style = SpanStyle(color = Color.Red)

    @Test
    fun `빈 query면 plain text 그대로 반환하고 span 없다`() {
        val result = highlightQuery("경기도 평택시 안중읍", "", style)
        assertThat(result.text).isEqualTo("경기도 평택시 안중읍")
        assertThat(result.spanStyles).isEmpty()
    }

    @Test
    fun `공백뿐인 query도 plain text 그대로 반환한다`() {
        val result = highlightQuery("경기도 평택시 안중읍", "   ", style)
        assertThat(result.spanStyles).isEmpty()
    }

    @Test
    fun `단일 토큰 매치 시 정확한 range에 span을 적용한다`() {
        val result = highlightQuery("경기도 평택시 안중읍", "안중", style)
        assertThat(result.spanStyles).hasSize(1)
        val span = result.spanStyles[0]
        assertThat(span.item).isEqualTo(style)
        assertThat(span.start).isEqualTo(8)
        assertThat(span.end).isEqualTo(10)
    }

    @Test
    fun `같은 토큰이 여러 번 나오면 모두 강조한다`() {
        val result = highlightQuery("경기도 평택시 안중읍 안중리", "안중", style)
        assertThat(result.spanStyles).hasSize(2)
        assertThat(result.spanStyles.map { it.start }).containsExactly(8, 12).inOrder()
    }

    @Test
    fun `여러 토큰을 공백으로 분리해 각각 강조한다`() {
        val result = highlightQuery("경기도 평택시 안중읍", "평택 안중", style)
        assertThat(result.spanStyles).hasSize(2)
        // 평택과 안중 각각 하나씩 span (시작 위치 무관, set 검증).
        val starts = result.spanStyles.map { it.start }.toSet()
        assertThat(starts).containsExactly(4, 8)
    }

    @Test
    fun `case insensitive 매칭 — 대소문자 다른 영문도 잡는다`() {
        val result = highlightQuery("Anjung-eup, Pyeongtaek-si, Gyeonggi-do", "anjung", style)
        assertThat(result.spanStyles).hasSize(1)
        assertThat(result.spanStyles[0].start).isEqualTo(0)
    }

    @Test
    fun `매칭 없으면 span 없다`() {
        val result = highlightQuery("경기도 평택시 안중읍", "서울", style)
        assertThat(result.spanStyles).isEmpty()
        assertThat(result.text).isEqualTo("경기도 평택시 안중읍")
    }
}
