package com.jewan.zipkr.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.jewan.zipkr.BuildConfig
import com.jewan.zipkr.data.provider.juso.JusoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
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
    fun provideOkHttpClient(): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }
        return OkHttpClient
            .Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
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

    private const val JUSO_BASE_URL = "https://business.juso.go.kr/"
}
