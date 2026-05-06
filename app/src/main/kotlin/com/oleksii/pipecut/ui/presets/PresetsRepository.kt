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

    suspend fun saveIfAllowed(
        preset: Preset,
        maxPresets: Int,
        allowOverwrite: Boolean,
    ): PresetSaveError?

    @Deprecated(
        message = "Use saveIfAllowed for race-free, capped saves. Will be removed in PR12.",
        level = DeprecationLevel.WARNING,
    )
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
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    override suspend fun saveIfAllowed(
        preset: Preset,
        maxPresets: Int,
        allowOverwrite: Boolean,
    ): PresetSaveError? {
        val normalizedName = preset.name.trim()
        if (normalizedName.isEmpty()) return PresetSaveError.EmptyName
        if (normalizedName.length > MAX_NAME_LENGTH) return PresetSaveError.NameTooLong
        val normalizedPreset =
            if (preset.name == normalizedName) preset else preset.copy(name = normalizedName)

        var error: PresetSaveError? = null
        store.edit { prefs ->
            // Self-heal: drop any preset key whose JSON does not
            // decode. Without this, corrupt entries occupy slots
            // forever — invisible in the UI yet counted toward the
            // cap. PR12 owns user-visible recovery affordances.
            val presetEntries = prefs.asMap()
                .filter { (k, v) -> k.name.startsWith(KEY_PREFIX) && v is String }
            val corruptKeys = presetEntries.mapNotNull { (k, v) ->
                val asString = v as String
                if (runCatching { json.decodeFromString<Preset>(asString) }.isFailure) k
                else null
            }
            for (k in corruptKeys) prefs.remove(k)

            val validEntries = prefs.asMap()
                .filter { (k, v) -> k.name.startsWith(KEY_PREFIX) && v is String }
            val matching = validEntries.keys.firstOrNull { key ->
                key.name.removePrefix(KEY_PREFIX)
                    .equals(normalizedPreset.name, ignoreCase = true)
            }
            if (matching != null && !allowOverwrite) {
                error = PresetSaveError.NameAlreadyExists
                return@edit
            }
            val isOverwrite = matching != null
            val postSize = if (isOverwrite) validEntries.size else validEntries.size + 1
            if (postSize > maxPresets) {
                error = PresetSaveError.LimitReached
                return@edit
            }
            if (matching != null) prefs.remove(matching)
            val key = stringPreferencesKey(KEY_PREFIX + normalizedPreset.name)
            prefs[key] = json.encodeToString(Preset.serializer(), normalizedPreset)
        }
        return error
    }

    @Deprecated(
        message = "Use saveIfAllowed for race-free, capped saves. Will be removed in PR12.",
        level = DeprecationLevel.WARNING,
    )
    override suspend fun save(preset: Preset) {
        val key = stringPreferencesKey(KEY_PREFIX + preset.name)
        store.edit { prefs ->
            prefs[key] = json.encodeToString(Preset.serializer(), preset)
        }
    }

    override suspend fun delete(name: String) {
        store.edit { prefs ->
            val match = prefs.asMap().keys.firstOrNull { key ->
                key.name.startsWith(KEY_PREFIX) &&
                    key.name.removePrefix(KEY_PREFIX).equals(name, ignoreCase = true)
            }
            if (match != null) prefs.remove(match)
        }
    }
}
