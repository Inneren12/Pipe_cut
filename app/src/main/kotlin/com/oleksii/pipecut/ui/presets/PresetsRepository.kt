package com.oleksii.pipecut.ui.presets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

interface PresetsRepository {
    fun observe(): Flow<List<Preset>>
    suspend fun save(preset: Preset)
    suspend fun delete(name: String)
}

private const val KEY_PREFIX = "preset/"
internal const val MAX_PRESETS = 20
internal const val MAX_NAME_LENGTH = 40

class DataStorePresetsRepository(
    private val store: DataStore<Preferences>,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : PresetsRepository {

    override fun observe(): Flow<List<Preset>> = store.data.map { prefs ->
        prefs.asMap()
            .mapNotNull { (key, value) ->
                if (!key.name.startsWith(KEY_PREFIX) || value !is String) null
                else runCatching { json.decodeFromString<Preset>(value) }.getOrNull()
            }
            .sortedBy { it.name }
    }

    override suspend fun save(preset: Preset) {
        val key = stringPreferencesKey(KEY_PREFIX + preset.name)
        store.edit { prefs ->
            prefs[key] = json.encodeToString(Preset.serializer(), preset)
        }
    }

    override suspend fun delete(name: String) {
        val key = stringPreferencesKey(KEY_PREFIX + name)
        store.edit { prefs ->
            prefs.remove(key)
        }
    }
}
