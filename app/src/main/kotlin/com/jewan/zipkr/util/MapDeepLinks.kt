package com.jewan.zipkr.util

import com.jewan.zipkr.data.Coordinate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * 외부 지도 앱 deep link / web fallback URL을 만든다.
 *
 * - 좌표가 있으면 정확한 핀 URL로, 없으면 주소 텍스트 query URL로 fallback한다.
 * - 카카오맵·네이버지도 두 앱 모두 같은 패턴이다.
 * - 앱 미설치 시 ActivityNotFoundException → 호출자에서 web fallback URL을 ACTION_VIEW로 처리.
 */
private fun encode(text: String): String = URLEncoder.encode(text, StandardCharsets.UTF_8.name())

fun kakaoMapDeepLink(
    coord: Coordinate?,
    fallbackAddress: String,
): String =
    if (coord != null) {
        "kakaomap://look?p=${coord.latitude},${coord.longitude}"
    } else {
        "kakaomap://search?q=${encode(fallbackAddress)}"
    }

fun naverMapDeepLink(
    coord: Coordinate?,
    name: String,
): String =
    if (coord != null) {
        "nmap://place?lat=${coord.latitude}&lng=${coord.longitude}&name=${encode(name)}"
    } else {
        "nmap://search?query=${encode(name)}"
    }

fun kakaoMapWebFallback(address: String): String = "https://map.kakao.com/?q=${encode(address)}"

fun naverMapWebFallback(address: String): String = "https://map.naver.com/?query=${encode(address)}"
