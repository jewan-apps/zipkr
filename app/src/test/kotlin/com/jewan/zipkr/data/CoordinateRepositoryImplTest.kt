package com.jewan.zipkr.data

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.provider.kakao.KakaoDocument
import com.jewan.zipkr.data.provider.kakao.KakaoGeocodeResponse
import com.jewan.zipkr.data.provider.kakao.KakaoLocalApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class CoordinateRepositoryImplTest {
    private val api: KakaoLocalApi = mockk()
    private val repo = CoordinateRepositoryImpl(api)

    @Test
    fun `정상 응답 시 첫 document를 Coordinate로 반환한다`() =
        runTest {
            coEvery { api.geocode("경기도 평택시 안현로 400") } returns
                KakaoGeocodeResponse(documents = listOf(KakaoDocument(x = "126.93", y = "36.98")))

            val result = repo.fetchCoordinate("경기도 평택시 안현로 400")

            val coord = (result as Result.Success).value
            assertThat(coord.longitude).isEqualTo(126.93)
            assertThat(coord.latitude).isEqualTo(36.98)
        }

    @Test
    fun `documents 빈 배열이면 AppError ApiBadResponse 실패를 반환한다`() =
        runTest {
            coEvery { api.geocode(any()) } returns KakaoGeocodeResponse(documents = emptyList())

            val result = repo.fetchCoordinate("없는 주소")

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val error = (result as Result.Failure).error
            assertThat(error).isInstanceOf(AppError.ApiBadResponse::class.java)
        }

    @Test
    fun `네트워크 IOException은 AppError Network로 wrap된다`() =
        runTest {
            coEvery { api.geocode(any()) } throws IOException("network down")

            val result = repo.fetchCoordinate("주소")

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            assertThat((result as Result.Failure).error).isInstanceOf(AppError.Network::class.java)
        }

    @Test
    fun `같은 주소를 두 번 호출하면 두 번째는 캐시 hit으로 API 호출 안 한다`() =
        runTest {
            coEvery { api.geocode("주소") } returns
                KakaoGeocodeResponse(documents = listOf(KakaoDocument(x = "1.0", y = "2.0")))

            repo.fetchCoordinate("주소")
            repo.fetchCoordinate("주소")

            coVerify(exactly = 1) { api.geocode("주소") }
        }
}
