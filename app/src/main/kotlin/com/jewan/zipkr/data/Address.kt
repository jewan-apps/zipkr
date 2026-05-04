package com.jewan.zipkr.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 도메인 주소 모델이다.
 * 외부 API DTO(JusoModels)와 분리되며, UI는 항상 이 모델만 본다.
 *
 * v1.1c부터 buildingName/sido/sigungu/eupmyeondong이 행안부 raw 응답에서 같이 매핑된다.
 * 빈 문자열도 허용한다(예: 건물명이 없는 일반 주택).
 *
 * @Parcelize는 SearchScreen → DetailSheet에 rememberSaveable로 통째 전달하기 위함이다.
 * @Serializable은 v1.1k에서 DataStore에 히스토리·즐겨찾기를 JSON으로 저장하기 위함이다.
 */
@Parcelize
@Serializable
data class Address(
    val zipCode: String,
    val roadAddress: String,
    val jibunAddress: String,
    val englishAddress: String,
    val buildingName: String,
    val sido: String,
    val sigungu: String,
    val eupmyeondong: String,
) : Parcelable {
    /**
     * 히스토리·즐겨찾기 중복 제거용 안정적인 키.
     * 같은 우편번호 + 도로명주소 + 건물명 조합이면 같은 entry로 취급한다.
     */
    val stableKey: String
        get() = "$zipCode|$roadAddress|$buildingName"
}
