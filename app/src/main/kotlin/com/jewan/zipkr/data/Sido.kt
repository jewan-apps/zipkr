package com.jewan.zipkr.data

/**
 * 행정구역 시·도이다.
 * displayName은 칩 라벨이고 apiPrefix는 검색 query 앞에 붙여 정확도를 높이는 prefix이다.
 * 행안부 API는 별도 sido 파라미터를 안정적으로 지원하지 않아 단순 prefix 방식을 택한다.
 *
 * "전체"는 별도 enum 값이 아니라 selectedSido = null로 표현한다 (sentinel 회피).
 */
enum class Sido(
    val displayName: String,
    val apiPrefix: String,
) {
    SEOUL("서울", "서울특별시"),
    BUSAN("부산", "부산광역시"),
    DAEGU("대구", "대구광역시"),
    INCHEON("인천", "인천광역시"),
    GWANGJU("광주", "광주광역시"),
    DAEJEON("대전", "대전광역시"),
    ULSAN("울산", "울산광역시"),
    SEJONG("세종", "세종특별자치시"),
    GYEONGGI("경기", "경기도"),
    GANGWON("강원", "강원특별자치도"),
    CHUNGBUK("충북", "충청북도"),
    CHUNGNAM("충남", "충청남도"),
    JEONBUK("전북", "전북특별자치도"),
    JEONNAM("전남", "전라남도"),
    GYEONGBUK("경북", "경상북도"),
    GYEONGNAM("경남", "경상남도"),
    JEJU("제주", "제주특별자치도"),
    ;

    companion object {
        /** UI 칩 row의 고정 순서이다. enum 선언 순서를 그대로 사용한다. */
        val ORDERED: List<Sido> = entries.toList()
    }
}
