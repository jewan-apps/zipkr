package com.jewan.zipkr.ui.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val repository: AddressRepository = mockk()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SearchViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 상태는 Idle이다`() =
        runTest {
            viewModel.uiState.test {
                val first = awaitItem()
                assertThat(first.query).isEmpty()
                assertThat(first.phase).isEqualTo(SearchUiState.Phase.Idle)
            }
        }

    @Test
    fun `검색 결과가 있으면 Success가 된다`() =
        runTest {
            val list = listOf(Address("06234", "도로명", "지번", "Eng"))
            coEvery { repository.search("강남") } returns Result.Success(list)

            viewModel.onQueryChange("강남")
            viewModel.searchNow()

            viewModel.uiState.test {
                val state = awaitItem()
                val phase = state.phase as SearchUiState.Phase.Success
                assertThat(phase.results).isEqualTo(list)
            }
        }

    @Test
    fun `검색 결과가 비면 Empty가 된다`() =
        runTest {
            coEvery { repository.search("zzz") } returns Result.Success(emptyList())

            viewModel.onQueryChange("zzz")
            viewModel.searchNow()

            viewModel.uiState.test {
                assertThat(awaitItem().phase).isEqualTo(SearchUiState.Phase.Empty)
            }
        }

    @Test
    fun `네트워크 실패면 Error가 된다`() =
        runTest {
            coEvery { repository.search("강남") } returns Result.Failure(AppError.Network())

            viewModel.onQueryChange("강남")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase
                assertThat(phase).isInstanceOf(SearchUiState.Phase.Error::class.java)
            }
        }

    @Test
    fun `5자리 숫자 입력 시 PostalCodeUnsupported가 되고 API 호출이 없다`() =
        runTest {
            // 우편번호 패턴 입력 — repository.search가 호출되지 않아야 한다.
            viewModel.onQueryChange("11823")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase
                assertThat(phase).isEqualTo(SearchUiState.Phase.PostalCodeUnsupported)
            }
            // API 호출이 없었음을 검증한다.
            coVerify(exactly = 0) { repository.search(any()) }
        }

    @Test
    fun `4자리 숫자 입력은 우편번호 가드에 걸리지 않는다`() =
        runTest {
            coEvery { repository.search("1234") } returns Result.Success(emptyList())

            viewModel.onQueryChange("1234")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase
                // 5자리 숫자만 우편번호로 간주한다 — 4자리는 일반 검색 흐름.
                assertThat(phase).isNotEqualTo(SearchUiState.Phase.PostalCodeUnsupported)
            }
        }

    @Test
    fun `숫자와 문자 혼합 입력은 우편번호 가드에 걸리지 않는다`() =
        runTest {
            coEvery { repository.search("1234a") } returns Result.Success(emptyList())

            viewModel.onQueryChange("1234a")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase
                assertThat(phase).isNotEqualTo(SearchUiState.Phase.PostalCodeUnsupported)
            }
        }

    @Test
    fun `ApiBadResponse E0006 응답 시 Phase Error에 AppError가 보존된다`() =
        runTest {
            val apiError = AppError.ApiBadResponse("E0006")
            coEvery { repository.search("서울") } returns Result.Failure(apiError)

            viewModel.onQueryChange("서울")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase as SearchUiState.Phase.Error
                assertThat(phase.error).isEqualTo(apiError)
                assertThat((phase.error as AppError.ApiBadResponse).isQueryTooBroad).isTrue()
            }
        }
}
