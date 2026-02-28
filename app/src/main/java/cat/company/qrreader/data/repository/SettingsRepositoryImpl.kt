package cat.company.qrreader.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "settings"

private val Context.dataStore by preferencesDataStore(
    name = DATASTORE_NAME,
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, "settings")) }
)

/**
 * Implementation of SettingsRepository using DataStore
 */
class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {
    private val HIDE_TAGGED_KEY = booleanPreferencesKey("hide_tagged_when_no_tag_selected")
    private val SEARCH_ACROSS_ALL_TAGS_KEY = booleanPreferencesKey("search_across_all_tags_when_filtering")
    private val AI_GENERATION_KEY = booleanPreferencesKey("ai_generation_enabled")
    private val AI_LANGUAGE_KEY = stringPreferencesKey("ai_language")
    private val AI_HUMOROUS_DESCRIPTIONS_KEY = booleanPreferencesKey("ai_humorous_descriptions")

    override val hideTaggedWhenNoTagSelected: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[HIDE_TAGGED_KEY] ?: false }

    override val searchAcrossAllTagsWhenFiltering: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[SEARCH_ACROSS_ALL_TAGS_KEY] ?: false }

    override val aiGenerationEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[AI_GENERATION_KEY] ?: true }

    override val aiLanguage: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[AI_LANGUAGE_KEY] ?: "device" }

    override val aiHumorousDescriptions: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[AI_HUMOROUS_DESCRIPTIONS_KEY] ?: false }

    override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HIDE_TAGGED_KEY] = value
        }
    }

    override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SEARCH_ACROSS_ALL_TAGS_KEY] = value
        }
    }

    override suspend fun setAiGenerationEnabled(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AI_GENERATION_KEY] = value
        }
    }

    override suspend fun setAiLanguage(value: String) {
        context.dataStore.edit { prefs ->
            prefs[AI_LANGUAGE_KEY] = value
        }
    }

    override suspend fun setAiHumorousDescriptions(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AI_HUMOROUS_DESCRIPTIONS_KEY] = value
        }
    }
}
