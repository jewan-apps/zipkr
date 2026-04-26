package com.jewan.zipkr.data

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.provider.AddressProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddressRepositoryImplTest {
    private val provider: AddressProvider = mockk()
    private val repository = AddressRepositoryImpl(provider = provider)

    @Test
    fun `search는 provider Success 결과를 그대로 전달한다`() =
        runTest {
            val expected =
                Result.Success(
                    AddressPage(
                        items = listOf(Address("06234", "도로명", "지번", "Eng")),
                        currentPage = 1,
                        totalCount = 1,
                    ),
                )
            coEvery { provider.search("강남", 1, 50) } returns expected

            val actual = repository.search("강남", page = 1, pageSize = 50)

            // Repository는 단순 위임이므로 동일 instance가 forwarding되어야 한다.
            assertThat(actual).isSameInstanceAs(expected)
        }

    @Test
    fun `search는 provider Failure 결과도 그대로 전달한다`() =
        runTest {
            val expected = Result.Failure(AppError.Network())
            coEvery { provider.search("강남", 1, 50) } returns expected

            val actual = repository.search("강남", page = 1, pageSize = 50)

            assertThat(actual).isSameInstanceAs(expected)
        }

    @Test
    fun `search는 page와 pageSize를 provider에 그대로 전달한다`() =
        runTest {
            val page =
                AddressPage(
                    items = emptyList(),
                    currentPage = 3,
                    totalCount = 200,
                )
            coEvery { provider.search("부산", 3, 50) } returns Result.Success(page)

            repository.search("부산", page = 3, pageSize = 50)

            // 인자가 그대로 위임되는지 검증한다.
            coVerify(exactly = 1) { provider.search("부산", 3, 50) }
        }
}
