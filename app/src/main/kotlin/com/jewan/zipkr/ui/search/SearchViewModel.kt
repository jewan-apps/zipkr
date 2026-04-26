package com.jewan.zipkr.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 검색 화면의 ViewModel이다.
 * 입력 변경에 debounce 700ms를 적용해 자동 검색을 트리거하며,
 * searchNow()로 즉시 트리거도 지원한다.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val repository: AddressRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SearchUiState())
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        private val queryFlow = MutableStateFlow("")
        private var inFlight: Job? = null

        // 마지막으로 트리거된 query를 추적해 debounce 흐름과 명시적 트리거(searchNow) 사이의
        // 중복 호출을 차단한다. 사용자가 키보드 돋보기를 누르면 즉시 검색되고, 700ms 후 debounce가
        // 같은 query를 다시 트리거하지 않도록 한다.
        private var lastTriggeredQuery: String? = null

        init {
            viewModelScope.launch {
                queryFlow
                    .debounce(DEBOUNCE_MS)
                    .distinctUntilChanged()
                    .filter { it.length >= MIN_QUERY_LEN }
                    .filter { it != lastTriggeredQuery }
                    .onEach { runSearch(it) }
                    .collect { /* no-op */ }
            }
        }

        fun onQueryChange(query: String) {
            _uiState.value = _uiState.value.copy(query = query)
            queryFlow.value = query
            if (query.length < MIN_QUERY_LEN) {
                _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.Idle)
            }
        }

        /**
         * 사용자가 명시적으로 즉시 검색을 누른 경우이다 (키보드 돋보기 / 재시도 버튼).
         * lastTriggeredQuery를 reset해 같은 query라도 강제 호출하며, 이후 debounce 중복 차단을
         * 함께 갱신한다.
         */
        fun searchNow() {
            lastTriggeredQuery = null
            runSearch(_uiState.value.query)
        }

        private fun runSearch(query: String) {
            inFlight?.cancel()
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.Idle)
                return
            }
            // 5자리 숫자 입력은 우편번호 역검색 시도로 판단해 API 호출 없이 안내 Phase로 전환한다.
            if (query.matches(POSTAL_CODE_PATTERN)) {
                lastTriggeredQuery = query
                _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.PostalCodeUnsupported)
                return
            }
            lastTriggeredQuery = query
            _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.Loading)
            inFlight =
                viewModelScope.launch {
                    val result = repository.search(query)
                    _uiState.value = _uiState.value.copy(phase = mapPhase(result))
                }
        }

        private fun mapPhase(result: Result<List<Address>>): SearchUiState.Phase =
            when (result) {
                is Result.Success -> {
                    if (result.value.isEmpty()) {
                        SearchUiState.Phase.Empty
                    } else {
                        SearchUiState.Phase.Success(result.value)
                    }
                }
                // AppError 자체를 보존한다 — 사용자 메시지는 Composable에서 stringResource로 매핑.
                is Result.Failure -> SearchUiState.Phase.Error(result.error)
            }

        private companion object {
            // 자동 검색 debounce 시간이다 (한국어 조합 입력을 고려해 700ms로 설정한다).
            const val DEBOUNCE_MS = 700L
            const val MIN_QUERY_LEN = 2

            // 5자리 숫자 입력은 우편번호 역검색 시도로 판단해 별도 Phase로 전환한다.
            // String.matches(Regex)는 full-match라 앵커 불필요.
            val POSTAL_CODE_PATTERN = Regex("""\d{5}""")
        }
    }
