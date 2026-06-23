package com.jewan.zipkr.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.jewan.zipkr.BuildConfig
import com.jewan.zipkr.data.provider.juso.JusoApi
import com.jewan.zipkr.data.provider.kakao.KakaoLocalApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

    // ---- 행안부 (Juso) ------------------------------------------------------

    @Provides
    @Singleton
    @Named(DiQualifiers.JUSO_OKHTTP)
    fun provideJusoOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideJusoRetrofit(
        @Named(DiQualifiers.JUSO_OKHTTP) client: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(JUSO_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideJusoApi(retrofit: Retrofit): JusoApi = retrofit.create(JusoApi::class.java)

    @Provides
    @Singleton
    @Named(DiQualifiers.JUSO_API_KEY)
    fun provideJusoApiKey(): String = BuildConfig.JUSO_API_KEY

    // ---- 카카오 (Kakao Local) -----------------------------------------------

    @Provides
    @Singleton
    @Named(DiQualifiers.KAKAO_REST_API_KEY)
    fun provideKakaoRestApiKey(): String = BuildConfig.KAKAO_REST_API_KEY

    @Provides
    @Singleton
    @Named(DiQualifiers.KAKAO_OKHTTP)
    fun provideKakaoOkHttpClient(
        @Named(DiQualifiers.KAKAO_REST_API_KEY) apiKey: String,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val authInterceptor =
            Interceptor { chain ->
                val req =
                    chain
                        .request()
                        .newBuilder()
                        .addHeader("Authorization", "KakaoAK $apiKey")
                        .build()
                chain.proceed(req)
            }
        return OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideKakaoLocalApi(
        @Named(DiQualifiers.KAKAO_OKHTTP) client: OkHttpClient,
        json: Json,
    ): KakaoLocalApi {
        val contentType = "application/json".toMediaType()
        return Retrofit
            .Builder()
            .baseUrl(KAKAO_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(KakaoLocalApi::class.java)
    }

    private const val JUSO_BASE_URL = "https://business.juso.go.kr/"
    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"
}
