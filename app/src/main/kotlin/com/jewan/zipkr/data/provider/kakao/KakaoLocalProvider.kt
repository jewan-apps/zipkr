package com.jewan.zipkr.data.provider.kakao

import com.jewan.zipkr.data.AddressPage
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Result
import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.juso.JusoApiProvider
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * 영문 keyword 검색을 위한 chain provider이다.
 *
 * 흐름:
 *   1. Kakao keyword API → 한글 road_address_name 후보 N건 추출
 *   2. 각 한글 도로명을 행안부 API에 전달 → 우편번호·영문주소·지번 정보 받음
 *   3. 결과 합쳐서 AddressPage로 반환 (dedup: 같은 stableKey 중복 제거)
 *
 * 도로명 단독 영문(예: "Teheran-ro 152")은 Kakao keyword가 잡지 못해 빈 결과로 떨어진다.
 * 이는 ADR-0002에서 명시된 의도된 한계이며, ViewModel에서 빈 결과 메시지 분기로 노출된다.
 *
 * 페이징 미지원: 영문 검색 결과는 첫 페이지만 노출 (Kakao keyword size=5 → 행안부 chain 5번).
 * v2.0에서 페이징 추가 시 Kakao의 page 파라미터와 호환 가능하다.
 */
class KakaoLocalProvider
    @Inject
    constructor(
        private val kakaoApi: KakaoLocalApi,
        private val jusoProvider: JusoApiProvider,
    ) : AddressProvider {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun search(
            query: String,
            page: Int,
            pageSize: Int,
        ): Result<AddressPage> =
            try {
                Timber.d("Kakao chain start: query=%s", query)
                val keywordResponse = kakaoApi.searchByKeyword(query)
                val roadAddresses =
                    keywordResponse.documents
                        .map { it.roadAddressName }
                        .filter { it.isNotBlank() }
                        .distinct()
                Timber.d("Kakao roadAddresses: %s", roadAddresses)

                if (roadAddresses.isEmpty()) {
                    Result.Success(AddressPage(items = emptyList(), currentPage = page, totalCount = 0))
                } else {
                    chainToJuso(roadAddresses, page, pageSize)
                }
            } catch (io: IOException) {
                Timber.w(io, "Kakao keyword chain network failure: query=%s", query)
                Result.Failure(AppError.Network(io))
            } catch (t: Throwable) {
                Timber.e(t, "Kakao keyword chain unknown failure: query=%s", query)
                Result.Failure(AppError.Unknown(t))
            }

        /**
         * Kakao 후보 도로명들을 행안부 API에 순차 전달하고 결과를 누적·dedup한다.
         * 하나라도 실패하면 그 단계의 에러를 그대로 전파한다 (전체 실패로 간주 — 일부 결과만 보여주면 사용자 혼란).
         */
        private suspend fun chainToJuso(
            roadAddresses: List<String>,
            page: Int,
            pageSize: Int,
        ): Result<AddressPage> {
            val collected = mutableListOf<com.jewan.zipkr.data.Address>()
            val seen = mutableSetOf<String>()
            for (road in roadAddresses) {
                val partial = jusoProvider.search(road, page = 1, pageSize = pageSize)
                if (partial is Result.Failure) return Result.Failure(partial.error)
                accumulateUnique((partial as Result.Success).value.items, collected, seen)
            }
            return Result.Success(
                AddressPage(items = collected, currentPage = page, totalCount = collected.size),
            )
        }

        private fun accumulateUnique(
            items: List<com.jewan.zipkr.data.Address>,
            collected: MutableList<com.jewan.zipkr.data.Address>,
            seen: MutableSet<String>,
        ) {
            items.forEach { addr -> if (seen.add(addr.stableKey)) collected += addr }
        }
    }
