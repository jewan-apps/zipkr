package com.jewan.zipkr.data.provider.kakao

import com.jewan.zipkr.data.Coordinate
import kotlinx.serialization.Serializable

/**
 * 카카오 로컬 REST geocoding API 응답 DTO이다.
 * 응답 필드는 풍부하지만 좌표 변환에 필요한 documents.x/y만 추출한다.
 */
@Serializable
data class KakaoGeocodeResponse(
    val documents: List<KakaoDocument> = emptyList(),
)

@Serializable
data class KakaoDocument(
    val x: String,
    val y: String,
) {
    /**
     * String으로 오는 좌표를 Double로 파싱한다.
     * 잘못된 숫자면 NumberFormatException — 호출자(Repository)에서 catch하여 AppError로 wrap한다.
     */
    fun toCoordinate(): Coordinate = Coordinate(longitude = x.toDouble(), latitude = y.toDouble())
}
