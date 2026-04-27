package com.jewan.zipkr.data.provider.juso

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.AddressPage
import org.junit.Test

class JusoModelsTest {
    @Test
    fun `JusoAddressDto는 Address 도메인 모델로 매핑된다`() {
        val dto =
            JusoAddressDto(
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
        val response =
            JusoSearchResponse(
                results =
                    JusoResults(
                        common = JusoCommon(errorCode = "0", errorMessage = "정상", totalCount = "1"),
                        juso =
                            listOf(
                                JusoAddressDto(
                                    roadAddr = "A",
                                    jibunAddr = "B",
                                    engAddr = "C",
                                    zipNo = "12345",
                                ),
                            ),
                    ),
            )
        assertThat(response.results.common.errorCode).isEqualTo("0")
        assertThat(response.results.juso).hasSize(1)
        // totalCount 필드 파싱 검증이다.
        assertThat(response.results.common.totalCount).isEqualTo("1")
    }

    @Test
    fun `AddressPage hasNext는 currentPage x pageSize가 totalCount보다 작을 때만 true이다`() {
        val pageWithMore =
            AddressPage(
                items = emptyList(),
                currentPage = 1,
                totalCount = 100,
            )
        val pageNoMore =
            AddressPage(
                items = emptyList(),
                currentPage = 2,
                totalCount = 100,
            )

        assertThat(pageWithMore.hasNext(pageSize = 50)).isTrue()
        assertThat(pageNoMore.hasNext(pageSize = 50)).isFalse()
    }

    @Test
    fun `totalCount가 없는 JusoCommon은 기본값 0을 갖는다`() {
        val common = JusoCommon(errorCode = "0", errorMessage = "정상")

        // 기본값 방어이다. 구버전 API 호환 시 0으로 처리되어 더 이상 페이지 없음으로 취급된다.
        assertThat(common.totalCount).isEqualTo("0")
    }

    @Test
    fun `toDomain 매핑 시 행안부 raw 응답의 bdNm·siNm·sggNm·emdNm가 Address 신규 필드로 들어간다`() {
        val dto =
            JusoAddressDto(
                roadAddr = "경기도 평택시 안중읍 안현로 400",
                jibunAddr = "경기도 평택시 안중읍 안중리 445-16 안중읍행정복지센터",
                engAddr = "400 Anhyeon-ro, Anjung-eup, Pyeongtaek-si, Gyeonggi-do",
                zipNo = "17941",
                bdNm = "안중읍행정복지센터",
                siNm = "경기도",
                sggNm = "평택시",
                emdNm = "안중읍",
            )

        val domain = dto.toDomain()

        assertThat(domain.zipCode).isEqualTo("17941")
        assertThat(domain.buildingName).isEqualTo("안중읍행정복지센터")
        assertThat(domain.sido).isEqualTo("경기도")
        assertThat(domain.sigungu).isEqualTo("평택시")
        assertThat(domain.eupmyeondong).isEqualTo("안중읍")
    }

    @Test
    fun `bdNm이 빈 문자열이면 Address buildingName도 빈 문자열이다`() {
        val dto =
            JusoAddressDto(
                roadAddr = "도로명",
                jibunAddr = "지번",
                engAddr = "Eng",
                zipNo = "12345",
                bdNm = "",
                siNm = "서울",
                sggNm = "강남구",
                emdNm = "역삼동",
            )

        assertThat(dto.toDomain().buildingName).isEqualTo("")
    }
}
