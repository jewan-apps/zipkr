package com.jewan.zipkr.data

import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.di.DiQualifiers
import javax.inject.Inject
import javax.inject.Named

/**
 * 도메인 레포지토리 구현체이다.
 *
 * 입력 query에 한글 char가 하나도 없으면 영문 검색 모드로 판단하고 영문 Provider(Kakao keyword chain)에 위임한다.
 * 한글이 한 char라도 있으면 기존 행안부 단독 흐름(한글 Provider) 그대로 — 한·영 혼합도 한글 모드로 분기.
 * 이는 외국인 영문 검색 패턴(POI 위주)이 보통 영문 100%인 점을 활용한 단순 룰로, 사용자 의식 없이 자연스럽게 분기된다.
 *
 * AddressProvider 인터페이스를 qualifier로 두 구현체에 바인딩해, Repository는 Provider 종류를 모르고 추상화만 사용한다 (양산 표준 패턴).
 */
class AddressRepositoryImpl
    @Inject
    constructor(
        @Named(DiQualifiers.KOREAN_PROVIDER) private val koreanProvider: AddressProvider,
        @Named(DiQualifiers.ENGLISH_PROVIDER) private val englishProvider: AddressProvider,
    ) : AddressRepository {
        override suspend fun search(
            query: String,
            page: Int,
            pageSize: Int,
        ): Result<AddressPage> =
            if (query.isEnglishOnly()) {
                englishProvider.search(query, page, pageSize)
            } else {
                koreanProvider.search(query, page, pageSize)
            }

        /**
         * 영문 흐름(Kakao chain) 분기 조건:
         *   1. 한글 char가 한 char도 없어야 한다.
         *   2. 영문 letter가 한 char라도 있어야 한다 (숫자 단독은 영문이 아님).
         *
         * 두 번째 조건은 5자리 우편번호 native 검색(v1.1f)과의 회귀를 막기 위해 필요하다 — "06236"은 영문 letter 없으므로
         * 한글 흐름(행안부)으로 보내져 native zipcode 검색이 동작한다.
         *
         * 예: "Gangnam Station" → 영문 / "강남역" → 한글 / "Gangnam 강남" → 한글 / "06236" → 한글.
         */
        private fun String.isEnglishOnly(): Boolean =
            none { it in '가'..'힣' || it in 'ㄱ'..'ㆎ' } && any { it.isLetter() }
    }
