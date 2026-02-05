package cat.company.qrreader.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "settings"

private val Context.dataStore by preferencesDataStore(
    name = DATASTORE_NAME,
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, "settings")) }
)

class SettingsRepository(private val context: Context) {
    private val HIDE_TAGGED_KEY = booleanPreferencesKey("hide_tagged_when_no_tag_selected")

    val hideTaggedWhenNoTagSelected: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[HIDE_TAGGED_KEY] ?: false }

    suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HIDE_TAGGED_KEY] = value
        }
    }
}
