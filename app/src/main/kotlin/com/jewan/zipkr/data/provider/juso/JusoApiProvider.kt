package com.jewan.zipkr.data.provider.juso

import com.jewan.zipkr.data.Address
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
        override suspend fun search(query: String): Result<List<Address>> =
            try {
                val response = api.search(apiKey = apiKey, keyword = query)
                val errorCode = response.results.common.errorCode
                if (errorCode == SUCCESS_CODE) {
                    Result.Success(response.results.juso.map { it.toDomain() })
                } else {
                    // 원문 errorMessage는 Timber 로그까지만 남기고 도메인 타입에는 담지 않는다 (헌법 §1.9).
                    Timber.w("Juso API non-success: code=%s msg=%s", errorCode, response.results.common.errorMessage)
                    Result.Failure(AppError.ApiBadResponse(code = errorCode))
                }
            } catch (io: IOException) {
                Timber.w(io, "Juso API network failure")
                Result.Failure(AppError.Network(io))
            } catch (t: Throwable) {
                Timber.e(t, "Juso API unknown failure")
                Result.Failure(AppError.Unknown(t))
            }

        private companion object {
            const val SUCCESS_CODE = "0"
        }
    }
