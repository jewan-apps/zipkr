package com.jewan.zipkr.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewan.zipkr.analytics.AnalyticsTracker
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.data.CoordinateRepository
import com.jewan.zipkr.data.Result
import com.jewan.zipkr.data.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 상세 시트의 ViewModel이다.
 * Address는 컴포저블 props로 받고, 좌표만 lazy fetch한다.
 * 같은 주소 재호출은 Repository의 in-memory 캐시가 흡수한다.
 *
 * v1.1k: 사용자가 detail sheet를 열면 자동으로 최근 본 주소에 누적되고, ★ 토글로 즐겨찾기 추가/해제가 가능하다.
 */
@HiltViewModel
class DetailViewModel
    @Inject
    constructor(
        private val coordinateRepository: CoordinateRepository,
        private val searchHistoryRepository: SearchHistoryRepository,
        private val analyticsTracker: AnalyticsTracker,
    ) : ViewModel() {
        private val _coordinate = MutableStateFlow<CoordinatePhase>(CoordinatePhase.Loading)
        val coordinate: StateFlow<CoordinatePhase> = _coordinate.asStateFlow()

        fun fetchCoordinate(roadAddress: String) {
            _coordinate.value = CoordinatePhase.Loading
            viewModelScope.launch {
                _coordinate.value =
                    when (val result = coordinateRepository.fetchCoordinate(roadAddress)) {
                        is Result.Success -> CoordinatePhase.Success(result.value)
                        is Result.Failure -> CoordinatePhase.Failure(result.error)
                    }
            }
        }

        /**
         * 시트 진입 시 호출 — 같은 주소를 다시 열어도 최근 list 맨 앞으로 끌어올린다.
         */
        fun rememberRecent(address: Address) {
            viewModelScope.launch { searchHistoryRepository.addRecent(address) }
        }

        fun toggleFavorite(address: Address) {
            viewModelScope.launch { searchHistoryRepository.toggleFavorite(address) }
        }

        fun trackCopyPostalCode() {
            analyticsTracker.trackCopyPostalCode()
        }

        /**
         * 본 주소가 즐겨찾기에 들어있는지 — ★ 토글의 채움 여부에 사용한다.
         */
        fun isFavoriteFlow(address: Address): Flow<Boolean> =
            searchHistoryRepository.observeFavorites().map { list -> list.any { it.stableKey == address.stableKey } }
    }
