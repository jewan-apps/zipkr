package com.jewan.zipkr.data.provider.juso

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 행안부 도로명주소 검색 API의 Retrofit 인터페이스이다.
 * Endpoint: https://business.juso.go.kr/addrlink/addrLinkApi.do
 */
interface JusoApi {
    @GET("addrlink/addrLinkApi.do?resultType=json")
    suspend fun search(
        @Query("confmKey") apiKey: String,
        @Query("keyword") keyword: String,
        @Query("currentPage") currentPage: Int = DEFAULT_PAGE,
        @Query("countPerPage") countPerPage: Int = DEFAULT_PAGE_SIZE,
    ): JusoSearchResponse

    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_PAGE_SIZE = 10
    }
}
