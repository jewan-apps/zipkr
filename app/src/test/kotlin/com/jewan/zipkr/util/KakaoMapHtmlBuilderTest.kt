package com.jewan.zipkr.util

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.Coordinate
import org.junit.Test

class KakaoMapHtmlBuilderTest {
    private val coord = Coordinate(longitude = 126.93, latitude = 36.98)

    @Test
    fun `HTML에 카카오맵 SDK script 태그가 들어가고 jsKey가 주입된다`() {
        val html = buildKakaoMapHtml(coord, jsKey = "TEST_KEY", level = 3)
        assertThat(html).contains("dapi.kakao.com/v2/maps/sdk.js")
        assertThat(html).contains("appkey=TEST_KEY")
        assertThat(html).contains("autoload=false")
    }

    @Test
    fun `HTML에 위·경도가 그대로 들어가고 마커가 추가된다`() {
        val html = buildKakaoMapHtml(coord, jsKey = "K", level = 3)
        assertThat(html).contains("36.98")
        assertThat(html).contains("126.93")
        assertThat(html).contains("Marker")
    }

    @Test
    fun `level 인자가 setLevel 호출에 반영된다`() {
        val html = buildKakaoMapHtml(coord, jsKey = "K", level = 5)
        assertThat(html).contains("level: 5")
    }
}
