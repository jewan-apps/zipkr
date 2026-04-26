package com.jewan.zipkr.data.provider.juso

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class JusoApiProviderTest {
    private val api: JusoApi = mockk()
    private val apiKey = "TEST_KEY"
    private val provider = JusoApiProvider(api = api, apiKey = apiKey)

    @Test
    fun `정상 응답이면 Address 리스트를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } returns
                JusoSearchResponse(
                    results =
                        JusoResults(
                            common = JusoCommon("0", "정상"),
                            juso =
                                listOf(
                                    JusoAddressDto("도로명1", "지번1", "Eng1", "11111"),
                                    JusoAddressDto("도로명2", "지번2", "Eng2", "22222"),
                                ),
                        ),
                )

            val result = provider.search("강남")

            assertThat(result).isInstanceOf(Result.Success::class.java)
            val list = (result as Result.Success).value
            assertThat(list).hasSize(2)
            assertThat(list.first().zipCode).isEqualTo("11111")
        }

    @Test
    fun `errorCode가 0이 아니면 ApiBadResponse를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } returns
                JusoSearchResponse(
                    results =
                        JusoResults(
                            common = JusoCommon("E0006", "API 키 오류"),
                            juso = emptyList(),
                        ),
                )

            val result = provider.search("강남")

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val error = (result as Result.Failure).error
            assertThat(error).isInstanceOf(AppError.ApiBadResponse::class.java)
            assertThat((error as AppError.ApiBadResponse).code).isEqualTo("E0006")
        }

    @Test
    fun `IOException이면 Network 에러를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } throws IOException("no internet")

            val result = provider.search("강남")

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val error = (result as Result.Failure).error
            assertThat(error).isInstanceOf(AppError.Network::class.java)
        }

    @Test
    fun `기타 예외는 Unknown 에러를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } throws RuntimeException("boom")

            val result = provider.search("강남")

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val error = (result as Result.Failure).error
            assertThat(error).isInstanceOf(AppError.Unknown::class.java)
        }
}
