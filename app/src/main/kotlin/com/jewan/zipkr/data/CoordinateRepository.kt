package com.jewan.zipkr.data

/**
 * 도로명주소 → WGS84 좌표 변환 레포지토리이다.
 * UI는 본 인터페이스만 알며, 카카오 Provider 교체 가능성은 Impl 단에서 흡수된다.
 */
interface CoordinateRepository {
    /**
     * 도로명주소로 좌표를 조회한다.
     * 같은 주소 재호출은 in-memory 캐시 hit으로 즉시 반환한다.
     */
    suspend fun fetchCoordinate(roadAddress: String): Result<Coordinate>
}
