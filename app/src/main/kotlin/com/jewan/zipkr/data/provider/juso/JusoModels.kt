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
)

@Serializable
data class JusoAddressDto(
    @SerialName("roadAddr") val roadAddr: String,
    @SerialName("jibunAddr") val jibunAddr: String,
    @SerialName("engAddr") val engAddr: String,
    @SerialName("zipNo") val zipNo: String,
) {
    /** API DTO를 도메인 모델로 변환한다. */
    fun toDomain(): Address = Address(
        zipCode = zipNo,
        roadAddress = roadAddr,
        jibunAddress = jibunAddr,
        englishAddress = engAddr,
    )
}
