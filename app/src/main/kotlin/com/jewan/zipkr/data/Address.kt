package com.jewan.zipkr.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 도메인 주소 모델이다.
 * 외부 API DTO(JusoModels)와 분리되며, UI는 항상 이 모델만 본다.
 *
 * v1.1c부터 buildingName/sido/sigungu/eupmyeondong이 행안부 raw 응답에서 같이 매핑된다.
 * 빈 문자열도 허용한다(예: 건물명이 없는 일반 주택).
 *
 * @Parcelize는 SearchScreen → DetailSheet에 rememberSaveable로 통째 전달하기 위함이다.
 */
@Parcelize
data class Address(
    val zipCode: String,
    val roadAddress: String,
    val jibunAddress: String,
    val englishAddress: String,
    val buildingName: String,
    val sido: String,
    val sigungu: String,
    val eupmyeondong: String,
) : Parcelable
