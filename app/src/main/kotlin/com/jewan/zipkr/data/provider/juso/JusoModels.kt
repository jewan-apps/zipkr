package com.jewan.zipkr.data.provider.juso

import com.jewan.zipkr.data.Address
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 행안부 도로명주소 API 응답 DTO이다.
 * (참고) https://business.juso.go.kr/addrlink/openApi/searchApi.do
 */
@Serializable
data class JusoSearchResponse(
    val results: JusoResults,
)

@Serializable
data class JusoResults(
    val common: JusoCommon,
    val juso: List<JusoAddressDto> = emptyList(),
)

@Serializable
data class JusoCommon(
    val errorCode: String,
    val errorMessage: String,
    // 행안부 API는 totalCount를 String으로 반환한다. toIntOrNull()로 파싱은 Provider 단에서 수행한다.
    val totalCount: String = "0",
)

@Serializable
data class JusoAddressDto(
    @SerialName("roadAddr") val roadAddr: String,
    @SerialName("jibunAddr") val jibunAddr: String,
    @SerialName("engAddr") val engAddr: String,
    @SerialName("zipNo") val zipNo: String,
    // v1.1c: 상세 시트가 행정구역/건물명을 별도로 보여주기 위한 raw 필드이다.
    // 행안부 응답에 따라 누락 가능 → default ""로 deserialization 안전성 확보.
    @SerialName("bdNm") val bdNm: String = "",
    @SerialName("siNm") val siNm: String = "",
    @SerialName("sggNm") val sggNm: String = "",
    @SerialName("emdNm") val emdNm: String = "",
) {
    /** API DTO를 도메인 모델로 변환한다. */
    fun toDomain(): Address =
        Address(
            zipCode = zipNo,
            roadAddress = roadAddr,
            jibunAddress = jibunAddr,
            englishAddress = engAddr,
            buildingName = bdNm,
            sido = siNm,
            sigungu = sggNm,
            eupmyeondong = emdNm,
        )
}
