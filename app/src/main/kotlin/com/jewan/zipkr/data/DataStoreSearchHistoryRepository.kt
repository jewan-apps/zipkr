package com.jewan.zipkr.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jewan.zipkr.data.SearchHistoryRepository.Companion.RECENT_LIMIT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore Preferences 기반 [SearchHistoryRepository] 구현이다.
 *
 * - 두 list를 각각 JSON 직렬화한 String으로 저장한다 (Room 도입 없이도 충분히 가벼운 구조).
 * - JSON 파싱 실패 시(예: 모델 구조 변경) 빈 list로 graceful degradation — 사용자 경험을 막지 않는다.
 */
@Singleton
class DataStoreSearchHistoryRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SearchHistoryRepository {
        private val json = Json { ignoreUnknownKeys = true }
        private val listSerializer = ListSerializer(Address.serializer())

        override fun observeRecent(): Flow<List<Address>> = dataStore.data.map { it.readList(KEY_RECENT) }

        override fun observeFavorites(): Flow<List<Address>> = dataStore.data.map { it.readList(KEY_FAVORITES) }

        override suspend fun addRecent(address: Address) {
            dataStore.edit { prefs ->
                val current = prefs.readList(KEY_RECENT)
                // 이미 있으면 맨 앞으로 끌어올리고, 없으면 prepend. 항상 RECENT_LIMIT 이하로 잘라낸다.
                val withoutDuplicate = current.filterNot { it.stableKey == address.stableKey }
                val updated = (listOf(address) + withoutDuplicate).take(RECENT_LIMIT)
                prefs[KEY_RECENT] = json.encodeToString(listSerializer, updated)
            }
        }

        override suspend fun toggleFavorite(address: Address) {
            dataStore.edit { prefs ->
                val current = prefs.readList(KEY_FAVORITES)
                val isFavorite = current.any { it.stableKey == address.stableKey }
                val updated =
                    if (isFavorite) {
                        current.filterNot { it.stableKey == address.stableKey }
                    } else {
                        current + address
                    }
                prefs[KEY_FAVORITES] = json.encodeToString(listSerializer, updated)
            }
        }

        override suspend fun removeRecent(address: Address) = removeFromList(KEY_RECENT, address)

        override suspend fun removeFavorite(address: Address) = removeFromList(KEY_FAVORITES, address)

        private suspend fun removeFromList(
            key: Preferences.Key<String>,
            address: Address,
        ) {
            dataStore.edit { prefs ->
                val updated = prefs.readList(key).filterNot { it.stableKey == address.stableKey }
                prefs[key] = json.encodeToString(listSerializer, updated)
            }
        }

        private fun Preferences.readList(key: Preferences.Key<String>): List<Address> {
            val raw = this[key] ?: return emptyList()
            return try {
                json.decodeFromString(listSerializer, raw)
            } catch (_: SerializationException) {
                // 모델 변경·손상 시 영구 차단 대신 빈 list로 회복한다.
                emptyList()
            }
        }

        private companion object {
            val KEY_RECENT = stringPreferencesKey("recent_addresses_json")
            val KEY_FAVORITES = stringPreferencesKey("favorite_addresses_json")
        }
    }
