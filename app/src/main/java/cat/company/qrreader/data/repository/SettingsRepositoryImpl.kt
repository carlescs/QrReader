package cat.company.qrreader.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    override val hideTaggedWhenNoTagSelected: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[HIDE_TAGGED_KEY] ?: false }

    override val searchAcrossAllTagsWhenFiltering: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[SEARCH_ACROSS_ALL_TAGS_KEY] ?: false }

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
}
