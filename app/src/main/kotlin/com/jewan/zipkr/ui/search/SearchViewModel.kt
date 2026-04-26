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
 * 입력 변경에 debounce 400ms를 적용해 자동 검색을 트리거하며,
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

        init {
            viewModelScope.launch {
                queryFlow
                    .debounce(DEBOUNCE_MS)
                    .distinctUntilChanged()
                    .filter { it.length >= MIN_QUERY_LEN }
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

        /** 사용자가 명시적으로 즉시 검색을 누른 경우이다. */
        fun searchNow() {
            runSearch(_uiState.value.query)
        }

        private fun runSearch(query: String) {
            inFlight?.cancel()
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(phase = SearchUiState.Phase.Idle)
                return
            }
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
            const val DEBOUNCE_MS = 400L
            const val MIN_QUERY_LEN = 2
        }
    }
