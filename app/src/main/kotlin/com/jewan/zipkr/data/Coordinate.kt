package com.jewan.zipkr.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * WGS84 위·경도 좌표이다.
 * 카카오 응답의 x = longitude, y = latitude로 매핑된다.
 * @Parcelize는 시트 state에 들어갈 가능성을 위해 둔다.
 */
@Parcelize
data class Coordinate(
    val longitude: Double,
    val latitude: Double,
) : Parcelable
