package com.jewan.zipkr.data.provider.juso

import com.jewan.zipkr.data.AddressPage
import com.jewan.zipkr.data.AppError
import com.jewan.zipkr.data.Result
import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.di.DiQualifiers
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * 행안부 도로명주소 API 기반 AddressProvider 구현체이다.
 * 호출 → DTO 매핑 → 도메인 에러 매핑까지 책임진다.
 */
class JusoApiProvider
    @Inject
    constructor(
        private val api: JusoApi,
        @Named(DiQualifiers.JUSO_API_KEY) private val apiKey: String,
    ) : AddressProvider {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun search(
            query: String,
            page: Int,
            pageSize: Int,
        ): Result<AddressPage> =
            try {
                Timber.d("Juso API request: query=%s page=%d size=%d", query, page, pageSize)
                val response = api.search(apiKey = apiKey, keyword = query, currentPage = page, countPerPage = pageSize)
                val common = response.results.common
                val errorCode = common.errorCode
                val totalCount = common.totalCount.toIntOrNull() ?: 0
                Timber.d(
                    "Juso API response: code=%s msg=%s total=%d",
                    errorCode,
                    common.errorMessage,
                    totalCount,
                )
                if (errorCode == SUCCESS_CODE) {
                    Result.Success(
                        AddressPage(
                            items = response.results.juso.map { it.toDomain() },
                            currentPage = page,
                            totalCount = totalCount,
                        ),
                    )
                } else {
                    // 원문 errorMessage는 도메인 타입에 담지 않는다 (헌법 §1.9). Timber 디버그 로그까지만 남는다.
                    Result.Failure(AppError.ApiBadResponse(code = errorCode))
                }
            } catch (io: IOException) {
                Timber.w(io, "Juso API network failure: query=%s page=%d", query, page)
                Result.Failure(AppError.Network(io))
            } catch (t: Throwable) {
                Timber.e(t, "Juso API unknown failure: query=%s page=%d", query, page)
                Result.Failure(AppError.Unknown(t))
            }

        private companion object {
            const val SUCCESS_CODE = "0"
        }
    }
