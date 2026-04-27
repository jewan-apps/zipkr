package com.jewan.zipkr.data.provider.kakao

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test

class KakaoModelsTest {
    @Test
    fun `KakaoDocument의 x·y 문자열을 Coordinate WGS84로 변환한다`() {
        val doc = KakaoDocument(x = "126.931266170341", y = "36.9860624565805")

        val coord = doc.toCoordinate()

        assertThat(coord.longitude).isEqualTo(126.931266170341)
        assertThat(coord.latitude).isEqualTo(36.9860624565805)
    }

    @Test
    fun `KakaoDocument에 잘못된 숫자 문자열이 들어오면 NumberFormatException을 던진다`() {
        val doc = KakaoDocument(x = "abc", y = "36.98")

        try {
            doc.toCoordinate()
            fail("예외가 발생해야 한다")
        } catch (_: NumberFormatException) {
            // 기대된 예외이다.
        }
    }
}
