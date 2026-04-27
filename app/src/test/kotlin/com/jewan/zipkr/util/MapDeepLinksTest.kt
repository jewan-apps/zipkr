package com.jewan.zipkr.util

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.Coordinate
import org.junit.Test

class MapDeepLinksTest {
    private val coord = Coordinate(longitude = 126.93, latitude = 36.98)
    private val address = "경기도 평택시 안현로 400"

    @Test
    fun `좌표가 있으면 카카오맵 deep link는 lat lng를 사용한다`() {
        val link = kakaoMapDeepLink(coord = coord, fallbackAddress = address)
        assertThat(link).startsWith("kakaomap://look?p=")
        assertThat(link).contains("36.98,126.93")
    }

    @Test
    fun `좌표가 null이면 카카오맵 deep link는 search query로 fallback한다`() {
        val link = kakaoMapDeepLink(coord = null, fallbackAddress = address)
        assertThat(link).startsWith("kakaomap://search?q=")
        assertThat(link).contains("%EA%B2%BD%EA%B8%B0")
    }

    @Test
    fun `좌표가 있으면 네이버지도 deep link는 lat lng + name을 사용한다`() {
        val link = naverMapDeepLink(coord = coord, name = address)
        assertThat(link).startsWith("nmap://place?")
        assertThat(link).contains("lat=36.98")
        assertThat(link).contains("lng=126.93")
    }

    @Test
    fun `좌표가 null이면 네이버지도 deep link는 query로 fallback한다`() {
        val link = naverMapDeepLink(coord = null, name = address)
        assertThat(link).startsWith("nmap://search?")
        assertThat(link).contains("query=")
    }

    @Test
    fun `web fallback URL은 좌표 없이도 카카오맵 검색을 연다`() {
        val link = kakaoMapWebFallback(address)
        assertThat(link).startsWith("https://map.kakao.com/")
    }
}
