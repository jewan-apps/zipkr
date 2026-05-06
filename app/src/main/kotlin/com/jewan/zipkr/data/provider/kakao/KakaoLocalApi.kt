package com.jewan.zipkr.data.provider.kakao

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 카카오 로컬 REST API의 두 endpoint를 묶은 인터페이스이다.
 * 인증: Authorization header (NetworkModule의 OkHttp 인터셉터에서 주입)
 *
 * - geocode: 한글 도로명주소 → 좌표(상세 시트의 카카오맵 진입용)
 * - searchByKeyword: 영문 POI keyword → 한글 도로명주소(외국인 영문 검색 chain의 1단계)
 */
interface KakaoLocalApi {
    @GET("v2/local/search/address.json")
    suspend fun geocode(
        @Query("query") query: String,
    ): KakaoGeocodeResponse

    /**
     * POI keyword search. 영문 입력("Gangnam Finance Center")을 한국 POI 인덱스에서 매칭해 한글 도로명주소를 반환한다.
     * 외국인 사용자가 건물명·역명·랜드마크 등 인지 가능한 명사로 검색 시 동작한다.
     * 도로명 단독 영문은 미지원 (Kakao API 한계).
     */
    @GET("v2/local/search/keyword.json")
    suspend fun searchByKeyword(
        @Query("query") query: String,
        @Query("size") size: Int = DEFAULT_KEYWORD_SIZE,
    ): KakaoKeywordResponse

    companion object {
        // Kakao keyword API의 size 최댓값은 15. MVP는 5건만 잡아 chain 호출 부담을 최소화한다.
        private const val DEFAULT_KEYWORD_SIZE = 5
    }
}
