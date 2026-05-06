package com.jewan.zipkr.di

import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AddressRepositoryImpl
import com.jewan.zipkr.data.CoordinateRepository
import com.jewan.zipkr.data.CoordinateRepositoryImpl
import com.jewan.zipkr.data.DataStoreSearchHistoryRepository
import com.jewan.zipkr.data.SearchHistoryRepository
import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.juso.JusoApiProvider
import com.jewan.zipkr.data.provider.kakao.KakaoLocalProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    /**
     * 한글 입력 흐름의 AddressProvider — 행안부 도로명주소 API 단독.
     */
    @Binds
    @Singleton
    @Named(DiQualifiers.KOREAN_PROVIDER)
    abstract fun bindKoreanAddressProvider(impl: JusoApiProvider): AddressProvider

    /**
     * 영문 입력 흐름의 AddressProvider — Kakao keyword API → 행안부 도로명주소 chain.
     */
    @Binds
    @Singleton
    @Named(DiQualifiers.ENGLISH_PROVIDER)
    abstract fun bindEnglishAddressProvider(impl: KakaoLocalProvider): AddressProvider

    @Binds
    @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository

    @Binds
    @Singleton
    abstract fun bindCoordinateRepository(impl: CoordinateRepositoryImpl): CoordinateRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: DataStoreSearchHistoryRepository): SearchHistoryRepository
}
