package com.jewan.zipkr.data.provider.juso

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.AddressPage
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
    fun `정상 응답이면 AddressPage를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } returns
                JusoSearchResponse(
                    results =
                        JusoResults(
                            common = JusoCommon("0", "정상", totalCount = "25"),
                            juso =
                                listOf(
                                    JusoAddressDto("도로명1", "지번1", "Eng1", "11111"),
                                    JusoAddressDto("도로명2", "지번2", "Eng2", "22222"),
                                ),
                        ),
                )

            val result = provider.search("강남", page = 1, pageSize = 50)

            assertThat(result).isInstanceOf(Result.Success::class.java)
            val page = (result as Result.Success<AddressPage>).value
            assertThat(page.items).hasSize(2)
            assertThat(page.items.first().zipCode).isEqualTo("11111")
            // currentPage는 호출자가 전달한 page 인자가 보존되어야 한다.
            assertThat(page.currentPage).isEqualTo(1)
            // totalCount는 common.totalCount를 toIntOrNull로 파싱한 값이다.
            assertThat(page.totalCount).isEqualTo(25)
        }

    @Test
    fun `totalCount가 매핑되어 hasNext가 올바르게 동작한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } returns
                JusoSearchResponse(
                    results =
                        JusoResults(
                            common = JusoCommon("0", "정상", totalCount = "120"),
                            juso = listOf(JusoAddressDto("도로명", "지번", "Eng", "12345")),
                        ),
                )

            val result = provider.search("서울", page = 2, pageSize = 50)

            assertThat(result).isInstanceOf(Result.Success::class.java)
            val page = (result as Result.Success<AddressPage>).value
            assertThat(page.totalCount).isEqualTo(120)
            assertThat(page.currentPage).isEqualTo(2)
            // page 2, pageSize 50 → 100 < 120 이므로 hasNext = true이다.
            assertThat(page.hasNext(pageSize = 50)).isTrue()
        }

    @Test
    fun `errorCode가 0이 아니면 ApiBadResponse를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } returns
                JusoSearchResponse(
                    results =
                        JusoResults(
                            common = JusoCommon("E0006", "API 키 오류", totalCount = "0"),
                            juso = emptyList(),
                        ),
                )

            val result = provider.search("강남", page = 1, pageSize = 50)

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val error = (result as Result.Failure).error
            assertThat(error).isInstanceOf(AppError.ApiBadResponse::class.java)
            assertThat((error as AppError.ApiBadResponse).code).isEqualTo("E0006")
        }

    @Test
    fun `IOException이면 Network 에러를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } throws IOException("no internet")

            val result = provider.search("강남", page = 1, pageSize = 50)

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val error = (result as Result.Failure).error
            assertThat(error).isInstanceOf(AppError.Network::class.java)
        }

    @Test
    fun `기타 예외는 Unknown 에러를 반환한다`() =
        runTest {
            coEvery { api.search(any(), any(), any(), any()) } throws RuntimeException("boom")

            val result = provider.search("강남", page = 1, pageSize = 50)

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val error = (result as Result.Failure).error
            assertThat(error).isInstanceOf(AppError.Unknown::class.java)
        }
}
