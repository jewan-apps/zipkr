package com.jewan.zipkr.data

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.provider.AddressProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddressRepositoryImplTest {
    private val koreanProvider: AddressProvider = mockk()
    private val englishProvider: AddressProvider = mockk()
    private val repository =
        AddressRepositoryImpl(
            koreanProvider = koreanProvider,
            englishProvider = englishProvider,
        )

    @Test
    fun `한글 입력은 koreanProvider에 위임한다`() =
        runTest {
            val expected =
                Result.Success(
                    AddressPage(
                        items = listOf(Address("06234", "도로명", "지번", "Eng", "", "", "", "")),
                        currentPage = 1,
                        totalCount = 1,
                    ),
                )
            coEvery { koreanProvider.search("강남", 1, 50) } returns expected

            val actual = repository.search("강남", page = 1, pageSize = 50)

            assertThat(actual).isSameInstanceAs(expected)
            coVerify(exactly = 0) { englishProvider.search(any(), any(), any()) }
        }

    @Test
    fun `영문 단독 입력은 englishProvider에 위임한다`() =
        runTest {
            val expected =
                Result.Success(
                    AddressPage(
                        items = listOf(Address("06236", "테헤란로 152", "역삼동 737", "Teheran-ro 152", "", "", "", "")),
                        currentPage = 1,
                        totalCount = 1,
                    ),
                )
            coEvery { englishProvider.search("Gangnam Station", 1, 50) } returns expected

            val actual = repository.search("Gangnam Station", page = 1, pageSize = 50)

            assertThat(actual).isSameInstanceAs(expected)
            coVerify(exactly = 0) { koreanProvider.search(any(), any(), any()) }
        }

    @Test
    fun `한글 영문 혼합은 koreanProvider에 위임한다`() =
        runTest {
            // "Gangnam 강남" 같은 혼합 입력은 한글 char가 있으므로 한글 흐름으로 보낸다.
            val expected =
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))
            coEvery { koreanProvider.search("Gangnam 강남", 1, 50) } returns expected

            repository.search("Gangnam 강남", page = 1, pageSize = 50)

            coVerify(exactly = 1) { koreanProvider.search("Gangnam 강남", 1, 50) }
            coVerify(exactly = 0) { englishProvider.search(any(), any(), any()) }
        }

    @Test
    fun `영문 입력의 Failure도 그대로 전달된다`() =
        runTest {
            val expected = Result.Failure(AppError.Network())
            coEvery { englishProvider.search("Lotte World", 1, 50) } returns expected

            val actual = repository.search("Lotte World", page = 1, pageSize = 50)

            assertThat(actual).isSameInstanceAs(expected)
        }

    @Test
    fun `5자리 숫자만 입력은 우편번호 native 검색을 위해 koreanProvider로 분기된다`() =
        runTest {
            // v1.1f 우편번호 역검색은 행안부 API의 native 지원에 의존한다.
            // 영문 흐름(Kakao chain)으로 가면 POI 검색 빈 결과가 되므로 회귀 방지가 필수.
            val expected =
                Result.Success(
                    AddressPage(
                        items = listOf(Address("06236", "강남구 도로명", "역삼동 지번", "Eng", "", "", "", "")),
                        currentPage = 1,
                        totalCount = 1,
                    ),
                )
            coEvery { koreanProvider.search("06236", 1, 50) } returns expected

            val actual = repository.search("06236", page = 1, pageSize = 50)

            assertThat(actual).isSameInstanceAs(expected)
            coVerify(exactly = 0) { englishProvider.search(any(), any(), any()) }
        }
}
