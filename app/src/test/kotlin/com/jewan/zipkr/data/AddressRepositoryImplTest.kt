package com.jewan.zipkr.data

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.provider.AddressProvider
import io.mockk.coEvery
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
                    listOf(Address("06234", "도로명", "지번", "Eng")),
                )
            coEvery { provider.search("강남") } returns expected

            val actual = repository.search("강남")

            // Repository는 단순 위임이므로 동일 instance가 forwarding되어야 한다.
            assertThat(actual).isSameInstanceAs(expected)
        }

    @Test
    fun `search는 provider Failure 결과도 그대로 전달한다`() =
        runTest {
            val expected = Result.Failure(AppError.Network())
            coEvery { provider.search("강남") } returns expected

            val actual = repository.search("강남")

            assertThat(actual).isSameInstanceAs(expected)
        }
}
