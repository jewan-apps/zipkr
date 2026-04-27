package com.jewan.zipkr.data

import com.jewan.zipkr.data.provider.kakao.KakaoDocument
import com.jewan.zipkr.data.provider.kakao.KakaoLocalApi
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 카카오 로컬 geocoding 응답을 도메인 Coordinate로 변환하는 구현체이다.
 *
 * 캐시 정책: 동일 roadAddress 재조회 시 ConcurrentHashMap에서 즉시 반환.
 * MVP에선 무한 보존 (사용자 한 세션에 같은 주소를 여러 번 보는 시나리오 대응).
 * 메모리 압박 시 LruCache 교체는 후속 작업.
 */
@Singleton
class CoordinateRepositoryImpl
    @Inject
    constructor(
        private val api: KakaoLocalApi,
    ) : CoordinateRepository {
        private val cache = ConcurrentHashMap<String, Coordinate>()

        override suspend fun fetchCoordinate(roadAddress: String): Result<Coordinate> {
            val cached = cache[roadAddress]
            return if (cached != null) {
                Result.Success(cached)
            } else {
                runCatching { api.geocode(roadAddress) }
                    .fold(
                        onSuccess = { response -> handleSuccess(roadAddress, response.documents.firstOrNull()) },
                        onFailure = { throwable -> handleFailure(throwable) },
                    )
            }
        }

        private fun handleSuccess(
            roadAddress: String,
            first: KakaoDocument?,
        ): Result<Coordinate> {
            if (first == null) return Result.Failure(AppError.ApiBadResponse(EMPTY_DOCUMENTS_CODE))
            val coord = first.toCoordinate()
            cache[roadAddress] = coord
            return Result.Success(coord)
        }

        private fun handleFailure(throwable: Throwable): Result<Coordinate> =
            when (throwable) {
                is IOException -> Result.Failure(AppError.Network(cause = throwable))
                else -> Result.Failure(AppError.Unknown(cause = throwable))
            }

        private companion object {
            // 카카오가 200 OK를 주지만 documents 배열이 비어 매칭 실패한 경우의 도메인 코드이다.
            const val EMPTY_DOCUMENTS_CODE = "KAKAO_EMPTY"
        }
    }
