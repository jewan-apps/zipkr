package com.jewan.zipkr.data

/**
 * 도메인 주소 모델이다.
 * 외부 API DTO(JusoModels)와 분리되며, UI는 항상 이 모델만 본다.
 */
data class Address(
    val zipCode: String,
    val roadAddress: String,
    val jibunAddress: String,
    val englishAddress: String,
)
