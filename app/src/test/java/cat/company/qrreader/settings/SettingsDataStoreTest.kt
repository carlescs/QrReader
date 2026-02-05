package cat.company.qrreader.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SettingsDataStoreTest {
    private val KEY = booleanPreferencesKey("hide_tagged_when_no_tag_selected")

    @Test
    fun dataStore_persists_boolean_value() = runTest {
        val tempDirPath = kotlin.io.path.createTempDirectory("datastore-test")
        val tempDir = tempDirPath.toFile()
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = this,
            produceFile = { File(tempDir, "settings.preferences_pb") }
        )

        // test default (empty)
        val initial = dataStore.data.first()[KEY] ?: false
        assertFalse(initial)

        // write true
        dataStore.edit { prefs ->
            prefs[KEY] = true
        }

        // read back
        val after = dataStore.data.first()[KEY] ?: false
        assertTrue(after)
    }
}
