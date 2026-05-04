package com.jewan.zipkr.di

import com.jewan.zipkr.data.AddressRepository
import com.jewan.zipkr.data.AddressRepositoryImpl
import com.jewan.zipkr.data.CoordinateRepository
import com.jewan.zipkr.data.CoordinateRepositoryImpl
import com.jewan.zipkr.data.DataStoreSearchHistoryRepository
import com.jewan.zipkr.data.SearchHistoryRepository
import com.jewan.zipkr.data.provider.AddressProvider
import com.jewan.zipkr.data.provider.juso.JusoApiProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindAddressProvider(impl: JusoApiProvider): AddressProvider

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
