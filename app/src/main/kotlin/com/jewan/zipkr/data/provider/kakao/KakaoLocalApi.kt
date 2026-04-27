package com.jewan.zipkr.data.provider.kakao

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 카카오 로컬 REST API의 도로명주소 검색 endpoint이다.
 * Endpoint: https://dapi.kakao.com/v2/local/search/address.json
 * 인증: Authorization header (NetworkModule의 OkHttp 인터셉터에서 주입)
 */
interface KakaoLocalApi {
    @GET("v2/local/search/address.json")
    suspend fun geocode(
        @Query("query") query: String,
    ): KakaoGeocodeResponse
}
