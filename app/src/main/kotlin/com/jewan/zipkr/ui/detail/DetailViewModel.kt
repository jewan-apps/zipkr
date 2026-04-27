package com.jewan.zipkr.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewan.zipkr.data.CoordinateRepository
import com.jewan.zipkr.data.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 상세 시트의 ViewModel이다.
 * Address는 컴포저블 props로 받고, 좌표만 lazy fetch한다.
 * 같은 주소 재호출은 Repository의 in-memory 캐시가 흡수한다.
 */
@HiltViewModel
class DetailViewModel
    @Inject
    constructor(
        private val coordinateRepository: CoordinateRepository,
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
    }
