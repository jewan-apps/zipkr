package com.jewan.zipkr.ui.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Result
import io.mockk.coEvery
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
}
