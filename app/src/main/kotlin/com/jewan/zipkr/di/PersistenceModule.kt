package com.jewan.zipkr.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 영속 계층(DataStore Preferences) Hilt 모듈이다.
 * 검색 히스토리·즐겨찾기 단일 store를 application context에 묶어 제공한다.
 */
private const val SEARCH_HISTORY_STORE_NAME = "search_history"

private val Context.searchHistoryStore: DataStore<Preferences> by preferencesDataStore(name = SEARCH_HISTORY_STORE_NAME)

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    @Provides
    @Singleton
    fun provideSearchHistoryDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.searchHistoryStore
}
