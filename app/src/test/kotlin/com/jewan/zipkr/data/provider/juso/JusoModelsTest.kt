package com.jewan.zipkr.data.provider.juso

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class JusoModelsTest {

    @Test
    fun `JusoAddressDto는 Address 도메인 모델로 매핑된다`() {
        val dto = JusoAddressDto(
            roadAddr = "서울특별시 강남구 테헤란로 123",
            jibunAddr = "서울특별시 강남구 역삼동 736-1",
            engAddr = "123, Teheran-ro, Gangnam-gu, Seoul",
            zipNo = "06234",
        )
        val address = dto.toDomain()

        assertThat(address.zipCode).isEqualTo("06234")
        assertThat(address.roadAddress).isEqualTo("서울특별시 강남구 테헤란로 123")
        assertThat(address.jibunAddress).isEqualTo("서울특별시 강남구 역삼동 736-1")
        assertThat(address.englishAddress).isEqualTo("123, Teheran-ro, Gangnam-gu, Seoul")
    }

    @Test
    fun `JusoSearchResponse가 정상 코드면 results 리스트를 반환한다`() {
        val response = JusoSearchResponse(
            results = JusoResults(
                common = JusoCommon(errorCode = "0", errorMessage = "정상"),
                juso = listOf(
                    JusoAddressDto(
                        roadAddr = "A", jibunAddr = "B", engAddr = "C", zipNo = "12345",
                    )
                )
            )
        )
        assertThat(response.results.common.errorCode).isEqualTo("0")
        assertThat(response.results.juso).hasSize(1)
    }
}
