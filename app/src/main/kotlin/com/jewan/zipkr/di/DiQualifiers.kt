package com.jewan.zipkr.di

/**
 * Hilt @Named qualifier 키를 한 곳에 모은다.
 * 매직 스트링이 여러 파일에 흩어지면 오타로 DI 미스매치가 조용히 발생한다.
 * v2에서 다른 외부 키(KAKAO_API_KEY 등) 추가 시 본 파일에 const val 한 줄만 더한다.
 */
object DiQualifiers {
    const val JUSO_API_KEY = "juso_api_key"
    const val KAKAO_REST_API_KEY = "kakao_rest_api_key"
    const val JUSO_OKHTTP = "juso_okhttp"
    const val KAKAO_OKHTTP = "kakao_okhttp"

    // 한·영 입력에 따라 분기되는 AddressProvider 추상화 식별자.
    // KOREAN_PROVIDER → JusoApiProvider (행안부 단독), ENGLISH_PROVIDER → KakaoLocalProvider (Kakao keyword + 행안부 chain).
    const val KOREAN_PROVIDER = "korean_provider"
    const val ENGLISH_PROVIDER = "english_provider"
}
