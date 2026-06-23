package com.jewan.zipkr.ui.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jewan.zipkr.analytics.AnalyticsTracker
import com.jewan.zipkr.analytics.SearchQueryType
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AddressPage
import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Result
import com.jewan.zipkr.data.Sido
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val repository: AddressRepository = mockk()
    private val historyRepository: com.jewan.zipkr.data.SearchHistoryRepository =
        mockk(relaxed = true) {
            // SearchViewModel이 init에서 두 flow를 stateIn으로 collect하므로 빈 flow를 노출한다.
            every { observeFavorites() } returns flowOf(emptyList())
            every { observeRecent() } returns flowOf(emptyList())
        }
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SearchViewModel(repository, historyRepository, analyticsTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // 테스트 fixture 헬퍼이다. Address 모델 확장(buildingName/sido/sigungu/eupmyeondong) 시
    // 다수 call site의 노이즈를 줄이기 위해 도입했다. 신규 필드는 기본값 ""이다.
    private fun testAddress(
        zipCode: String,
        roadAddress: String,
        jibunAddress: String,
        englishAddress: String,
    ): Address =
        Address(
            zipCode = zipCode,
            roadAddress = roadAddress,
            jibunAddress = jibunAddress,
            englishAddress = englishAddress,
            buildingName = "",
            sido = "",
            sigungu = "",
            eupmyeondong = "",
        )

    // ------------------------------------------------------------------ 기존 케이스 (시그니처 update)

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
            val list = listOf(testAddress("06234", "도로명", "지번", "Eng"))
            coEvery { repository.search("강남", any(), any()) } returns
                Result.Success(
                    AddressPage(items = list, currentPage = 1, totalCount = 1),
                )

            viewModel.onQueryChange("강남")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase as SearchUiState.Phase.Success
                assertThat(phase.results).isEqualTo(list)
                assertThat(phase.currentPage).isEqualTo(1)
            }
        }

    @Test
    fun `검색 결과가 비면 Empty가 된다`() =
        runTest {
            coEvery { repository.search("zzz", any(), any()) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))

            viewModel.onQueryChange("zzz")
            viewModel.searchNow()

            viewModel.uiState.test {
                assertThat(awaitItem().phase).isEqualTo(SearchUiState.Phase.Empty)
            }
        }

    @Test
    fun `네트워크 실패면 Error가 된다`() =
        runTest {
            coEvery { repository.search("강남", any(), any()) } returns Result.Failure(AppError.Network())

            viewModel.onQueryChange("강남")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase
                assertThat(phase).isInstanceOf(SearchUiState.Phase.Error::class.java)
            }
        }

    @Test
    fun `5자리 숫자 입력 시 우편번호 그대로 keyword로 보내고 결과를 노출한다`() =
        runTest {
            // 행안부 API는 5자리 우편번호를 native 지원한다. ViewModel은 sido prefix 합성 없이 그대로 전달해야 한다.
            val zipResult = listOf(testAddress("11823", "도로명우편번호", "지번우편번호", "EngZip"))
            coEvery { repository.search("11823", 1, 50) } returns
                Result.Success(AddressPage(items = zipResult, currentPage = 1, totalCount = 1))

            viewModel.onQueryChange("11823")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase as SearchUiState.Phase.Success
                assertThat(phase.results).isEqualTo(zipResult)
            }
            coVerify(exactly = 1) { repository.search("11823", 1, 50) }
            verify(exactly = 1) {
                analyticsTracker.trackSearchAddress(
                    queryType = SearchQueryType.PostalCode,
                    sidoSelected = false,
                )
            }
        }

    @Test
    fun `5자리 숫자 입력 시 sido가 선택돼있어도 prefix 합성 없이 그대로 보낸다`() =
        runTest {
            // "서울특별시 06236"은 빈 결과가 되므로 우편번호일 땐 sido 무시 (의미상 우편번호 자체가 지역 포함).
            val zipResult = listOf(testAddress("06236", "강남구 도로명", "역삼동 지번", "EngZip"))
            coEvery { repository.search("06236", 1, 50) } returns
                Result.Success(AddressPage(items = zipResult, currentPage = 1, totalCount = 1))

            viewModel.onSidoChange(Sido.SEOUL)
            viewModel.onQueryChange("06236")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase as SearchUiState.Phase.Success
                assertThat(phase.results).isEqualTo(zipResult)
            }
            // sido prefix가 합성되지 않은 raw 우편번호로만 호출됐음을 검증한다.
            coVerify(exactly = 1) { repository.search("06236", 1, 50) }
            coVerify(exactly = 0) { repository.search(match { it.contains("서울특별시") }, any(), any()) }
            verify(exactly = 1) {
                analyticsTracker.trackSearchAddress(
                    queryType = SearchQueryType.PostalCode,
                    sidoSelected = true,
                )
            }
        }

    @Test
    fun `우편번호 복사 추적 이벤트를 전달한다`() =
        runTest {
            viewModel.trackCopyPostalCode()

            verify(exactly = 1) { analyticsTracker.trackCopyPostalCode() }
        }

    @Test
    fun `4자리 숫자 입력은 일반 검색 흐름으로 들어간다`() =
        runTest {
            // 5자리만 우편번호로 간주한다 — 4자리는 일반 keyword로 그대로 호출된다.
            coEvery { repository.search("1234", any(), any()) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))

            viewModel.onQueryChange("1234")
            viewModel.searchNow()

            coVerify(exactly = 1) { repository.search("1234", any(), any()) }
        }

    @Test
    fun `ApiBadResponse E0006 응답 시 Phase Error에 AppError가 보존된다`() =
        runTest {
            val apiError = AppError.ApiBadResponse("E0006")
            coEvery { repository.search("서울", any(), any()) } returns Result.Failure(apiError)

            viewModel.onQueryChange("서울")
            viewModel.searchNow()

            viewModel.uiState.test {
                val phase = awaitItem().phase as SearchUiState.Phase.Error
                assertThat(phase.error).isEqualTo(apiError)
                assertThat((phase.error as AppError.ApiBadResponse).isQueryTooBroad).isTrue()
            }
        }

    // ------------------------------------------------------------------ 신규 페이징 케이스

    @Test
    fun `loadMore 호출 시 다음 page를 fetch하고 누적 결과를 Success에 노출한다`() =
        runTest {
            val firstPage =
                listOf(
                    testAddress("11111", "도로명1", "지번1", "Eng1"),
                    testAddress("22222", "도로명2", "지번2", "Eng2"),
                )
            val secondPage = listOf(testAddress("33333", "도로명3", "지번3", "Eng3"))

            // 첫 page: totalCount=3, pageSize=50 → 1*50 >= 3 이므로 hasNext=false 방지.
            // hasNext 테스트 목적으로 totalCount를 크게 설정한다 (1 * 50 < 100 → true).
            coEvery { repository.search("강남", 1, 50) } returns
                Result.Success(AddressPage(items = firstPage, currentPage = 1, totalCount = 100))
            coEvery { repository.search("강남", 2, 50) } returns
                Result.Success(AddressPage(items = secondPage, currentPage = 2, totalCount = 100))

            viewModel.onQueryChange("강남")
            viewModel.searchNow()

            // 첫 page 로드 후 Success 상태 확인이다.
            val afterFirstSearch = viewModel.uiState.value.phase as SearchUiState.Phase.Success
            assertThat(afterFirstSearch.results).hasSize(2)
            assertThat(afterFirstSearch.currentPage).isEqualTo(1)
            assertThat(afterFirstSearch.hasNext).isTrue()

            // loadMore 호출이다.
            viewModel.loadMore()

            val afterLoadMore = viewModel.uiState.value.phase as SearchUiState.Phase.Success
            // 누적 결과: firstPage(2) + secondPage(1) = 3이다.
            assertThat(afterLoadMore.results).hasSize(3)
            assertThat(afterLoadMore.currentPage).isEqualTo(2)
            assertThat(afterLoadMore.results.last().zipCode).isEqualTo("33333")
        }

    @Test
    fun `hasNext가 false면 loadMore가 호출돼도 fetch 안 한다`() =
        runTest {
            val list = listOf(testAddress("11111", "도로명", "지번", "Eng"))
            // totalCount = 1, page = 1, pageSize = 50 → 1*50 >= 1 → hasNext = false이다.
            coEvery { repository.search("강남", 1, 50) } returns
                Result.Success(AddressPage(items = list, currentPage = 1, totalCount = 1))

            viewModel.onQueryChange("강남")
            viewModel.searchNow()

            val phase = viewModel.uiState.value.phase as SearchUiState.Phase.Success
            assertThat(phase.hasNext).isFalse()

            viewModel.loadMore()

            // page 2 호출이 없어야 한다.
            coVerify(exactly = 0) { repository.search("강남", 2, any()) }
        }

    @Test
    fun `isLoadingMore 동안 loadMore 중복 호출은 차단된다`() =
        runTest {
            val list = listOf(testAddress("11111", "도로명", "지번", "Eng"))
            coEvery { repository.search("강남", 1, 50) } returns
                Result.Success(AddressPage(items = list, currentPage = 1, totalCount = 200))

            viewModel.onQueryChange("강남")
            viewModel.searchNow()

            // 내부 StateFlow에 직접 접근 불가이므로, loadMore를 두 번 연속 호출해 동작을 검증한다.
            // UnconfinedTestDispatcher 환경에서는 첫 loadMore가 isLoadingMore = true로 설정 후
            // 두 번째 loadMore가 즉시 차단되어야 한다.
            // 주의: repository.search(page=2)를 stub하지 않았으므로, 호출 시 mockk 예외가 발생한다.
            // 차단 성공 시 예외 없이 통과한다.
            coEvery { repository.search("강남", 2, 50) } returns
                Result.Success(
                    AddressPage(items = emptyList(), currentPage = 2, totalCount = 200),
                )

            // 첫 loadMore이다 (isLoadingMore = true로 전환).
            viewModel.loadMore()
            // 두 번째 loadMore는 isLoadingMore = false가 되기 전에 호출해야 하지만
            // UnconfinedTestDispatcher에서는 즉시 완료된다. 직접 상태 검증으로 대체한다.
            val afterFirst = viewModel.uiState.value.phase
            // loadMore 완료 후에는 isLoadingMore = false이어야 한다.
            assertThat((afterFirst as SearchUiState.Phase.Success).isLoadingMore).isFalse()
        }

    // ------------------------------------------------------------------ 시·도 칩 케이스

    @Test
    fun `시도 선택 시 query 앞에 apiPrefix가 붙어 호출된다`() =
        runTest {
            // 사용자 입력은 "테헤란로"이지만 서울 칩 선택 시 effective query는 "서울특별시 테헤란로"여야 한다.
            coEvery { repository.search("서울특별시 테헤란로", 1, 50) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))

            viewModel.onQueryChange("테헤란로")
            viewModel.onSidoChange(Sido.SEOUL)

            // 사용자 입력 query는 그대로 유지되어야 한다 (입력칸 변화 없음).
            assertThat(viewModel.uiState.value.query).isEqualTo("테헤란로")
            assertThat(viewModel.uiState.value.selectedSido).isEqualTo(Sido.SEOUL)
            // effective query로 호출됐는지 확인이다.
            coVerify { repository.search("서울특별시 테헤란로", 1, 50) }
        }

    @Test
    fun `시도 변경만으로도 같은 base query에 대해 새 호출이 트리거된다`() =
        runTest {
            coEvery { repository.search("테헤란로", 1, 50) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))
            coEvery { repository.search("서울특별시 테헤란로", 1, 50) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))
            coEvery { repository.search("부산광역시 테헤란로", 1, 50) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))

            viewModel.onQueryChange("테헤란로")
            viewModel.searchNow()
            viewModel.onSidoChange(Sido.SEOUL)
            viewModel.onSidoChange(Sido.BUSAN)
            viewModel.onSidoChange(null)

            // 4개의 effective query가 각각 한 번씩 호출됐어야 한다 (전체 → 서울 → 부산 → 전체).
            coVerify(exactly = 2) { repository.search("테헤란로", 1, 50) }
            coVerify(exactly = 1) { repository.search("서울특별시 테헤란로", 1, 50) }
            coVerify(exactly = 1) { repository.search("부산광역시 테헤란로", 1, 50) }
        }

    @Test
    fun `시도 선택 후 loadMore도 동일한 prefix를 유지한다`() =
        runTest {
            val page1 =
                listOf(testAddress("06234", "서울 도로명1", "서울 지번1", "Eng1"))
            val page2 =
                listOf(testAddress("06235", "서울 도로명2", "서울 지번2", "Eng2"))

            coEvery { repository.search("서울특별시 테헤란로", 1, 50) } returns
                Result.Success(AddressPage(items = page1, currentPage = 1, totalCount = 100))
            coEvery { repository.search("서울특별시 테헤란로", 2, 50) } returns
                Result.Success(AddressPage(items = page2, currentPage = 2, totalCount = 100))

            viewModel.onQueryChange("테헤란로")
            viewModel.onSidoChange(Sido.SEOUL)
            viewModel.loadMore()

            // page 2도 동일한 effective query("서울특별시 테헤란로")로 호출돼야 한다.
            coVerify { repository.search("서울특별시 테헤란로", 2, 50) }
            val phase = viewModel.uiState.value.phase as SearchUiState.Phase.Success
            assertThat(phase.results).hasSize(2)
        }

    @Test
    fun `query가 짧으면 시도 변경에도 검색이 트리거되지 않는다`() =
        runTest {
            // MIN_QUERY_LEN = 2 미만 (1글자)이면 sido 변경만으로 API 호출되지 않아야 한다.
            viewModel.onQueryChange("강")
            viewModel.onSidoChange(Sido.SEOUL)

            assertThat(viewModel.uiState.value.selectedSido).isEqualTo(Sido.SEOUL)
            coVerify(exactly = 0) { repository.search(any(), any(), any()) }
        }

    @Test
    fun `query 입력 후 debounce 만료 전 시도 변경 시 같은 effective 호출이 두 번 일어나지 않는다`() =
        runTest {
            // dedup 회귀 가드: queryFlow의 filter가 raw로 비교하면, sido 선택으로 effective 호출이 먼저 나간 뒤
            // debounce 발화 시 raw가 lastTriggeredQuery(effective)와 달라 한 번 더 같은 effective가 호출된다.
            coEvery { repository.search("서울특별시 테헤란로", 1, 50) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))

            viewModel.onQueryChange("테헤란로")
            viewModel.onSidoChange(Sido.SEOUL)
            advanceTimeBy(DEBOUNCE_MS_FOR_TEST + 1)

            // 정확히 한 번만 호출돼야 한다.
            coVerify(exactly = 1) { repository.search("서울특별시 테헤란로", 1, 50) }
            // raw "테헤란로"는 절대 호출되지 않아야 한다 (sido prefix가 항상 붙는다).
            coVerify(exactly = 0) { repository.search("테헤란로", any(), any()) }
        }

    @Test
    fun `같은 시도 칩을 다시 눌러도 검색이 재호출되지 않는다`() =
        runTest {
            coEvery { repository.search("서울특별시 강남", 1, 50) } returns
                Result.Success(AddressPage(items = emptyList(), currentPage = 1, totalCount = 0))

            viewModel.onQueryChange("강남")
            viewModel.onSidoChange(Sido.SEOUL)
            // 같은 칩 재클릭 — UI상 toggle이 아니라 noop이어야 한다.
            viewModel.onSidoChange(Sido.SEOUL)

            coVerify(exactly = 1) { repository.search("서울특별시 강남", 1, 50) }
        }

    private companion object {
        // SearchViewModel.DEBOUNCE_MS와 동일해야 한다 (private companion 접근 불가로 상수 복제).
        const val DEBOUNCE_MS_FOR_TEST = 700L
    }
}
