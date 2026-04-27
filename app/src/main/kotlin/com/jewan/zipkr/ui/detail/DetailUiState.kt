package com.jewan.zipkr.ui.detail

import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Coordinate

/**
 * 상세 시트의 좌표 fetch 상태이다. Address 자체는 컴포저블 props로 받으므로 여기에 포함 안 한다.
 */
sealed interface CoordinatePhase {
    data object Loading : CoordinatePhase

    data class Success(
        val coordinate: Coordinate,
    ) : CoordinatePhase

    data class Failure(
        val error: AppError,
    ) : CoordinatePhase
}
