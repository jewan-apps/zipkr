package com.jewan.zipkr.ui.detail

import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Coordinate
import com.jewan.zipkr.data.CoordinateRepository
import com.jewan.zipkr.data.Result
import com.jewan.zipkr.data.SearchHistoryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
class DetailViewModelTest {
    private val repo: CoordinateRepository = mockk()
    private val historyRepo: SearchHistoryRepository =
        mockk(relaxed = true) {
            every { observeFavorites() } returns flowOf(emptyList())
            every { observeRecent() } returns flowOf(emptyList())
        }
    private lateinit var viewModel: DetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = DetailViewModel(repo, historyRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기 상태는 Loading이다`() =
        runTest {
            assertThat(viewModel.coordinate.value).isEqualTo(CoordinatePhase.Loading)
        }

    @Test
    fun `fetchCoordinate 성공 시 Success로 전환된다`() =
        runTest {
            coEvery { repo.fetchCoordinate("주소") } returns
                Result.Success(Coordinate(longitude = 126.0, latitude = 36.0))

            viewModel.fetchCoordinate("주소")

            val phase = viewModel.coordinate.value as CoordinatePhase.Success
            assertThat(phase.coordinate.longitude).isEqualTo(126.0)
        }

    @Test
    fun `fetchCoordinate 실패 시 Failure에 AppError가 보존된다`() =
        runTest {
            coEvery { repo.fetchCoordinate("주소") } returns Result.Failure(AppError.Network())

            viewModel.fetchCoordinate("주소")

            val phase = viewModel.coordinate.value as CoordinatePhase.Failure
            assertThat(phase.error).isInstanceOf(AppError.Network::class.java)
        }
}
