package cat.company.qrreader.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import cat.company.qrreader.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * Unit tests for SettingsRepository
 *
 * Note: Uses LooperMode.PAUSED to ensure DataStore operations complete properly in tests.
 * DataStore is a singleton per context, so tests may share state - each test should set up
 * its expected state rather than relying on default values.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
@LooperMode(LooperMode.Mode.PAUSED)
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        // Use Robolectric's application context
        // Each test gets its own isolated temp directory automatically
        context = ApplicationProvider.getApplicationContext()
        repository = SettingsRepositoryImpl(context)
    }

    @After
    fun tearDown() {
        // No manual cleanup needed - Robolectric handles temp directory cleanup
        // Attempting to delete DataStore files here can cause conflicts
    }

    /**
     * Test that we can set value to false and it persists
     */
    @Test
    fun hideTaggedWhenNoTagSelected_canBeSetToFalse() = runTest {
        // First set to false to ensure known state
        repository.setHideTaggedWhenNoTagSelected(false)

        // Then verify it reads back as false
        val result = repository.hideTaggedWhenNoTagSelected.first()
        assertFalse(result)
    }

    /**
     * Test setting hideTaggedWhenNoTagSelected to true
     */
    @Test
    fun setHideTaggedWhenNoTagSelected_true_updatesValue() = runTest {
        // Set to false first to ensure known state
        repository.setHideTaggedWhenNoTagSelected(false)
        assertFalse(repository.hideTaggedWhenNoTagSelected.first())

        // Set to true
        repository.setHideTaggedWhenNoTagSelected(true)

        // Verify it's now true
        val updatedValue = repository.hideTaggedWhenNoTagSelected.first()
        assertTrue(updatedValue)
    }

    /**
     * Test setting hideTaggedWhenNoTagSelected to false
     */
    @Test
    fun setHideTaggedWhenNoTagSelected_false_updatesValue() = runTest {
        // First set to true
        repository.setHideTaggedWhenNoTagSelected(true)
        val intermediateValue = repository.hideTaggedWhenNoTagSelected.first()
        assertTrue(intermediateValue)

        // Then set to false
        repository.setHideTaggedWhenNoTagSelected(false)

        // Verify it's now false
        val finalValue = repository.hideTaggedWhenNoTagSelected.first()
        assertFalse(finalValue)
    }

    /**
     * Test toggling the value multiple times
     */
    @Test
    fun setHideTaggedWhenNoTagSelected_multipleToggle_updatesCorrectly() = runTest {
        // Toggle to true
        repository.setHideTaggedWhenNoTagSelected(true)
        assertTrue(repository.hideTaggedWhenNoTagSelected.first())

        // Toggle to false
        repository.setHideTaggedWhenNoTagSelected(false)
        assertFalse(repository.hideTaggedWhenNoTagSelected.first())

        // Toggle to true again
        repository.setHideTaggedWhenNoTagSelected(true)
        assertTrue(repository.hideTaggedWhenNoTagSelected.first())

        // Toggle to false again
        repository.setHideTaggedWhenNoTagSelected(false)
        assertFalse(repository.hideTaggedWhenNoTagSelected.first())
    }

    /**
     * Test that the Flow emits values when changed
     */
    @Test
    fun hideTaggedWhenNoTagSelected_flowEmitsValues_whenChanged() = runTest {
        val values = mutableListOf<Boolean>()

        // Set to false first to ensure known state
        repository.setHideTaggedWhenNoTagSelected(false)
        val initialValue = repository.hideTaggedWhenNoTagSelected.first()
        values.add(initialValue)

        // Change value and wait for it to complete
        repository.setHideTaggedWhenNoTagSelected(true)
        val afterTrue = repository.hideTaggedWhenNoTagSelected.first()
        values.add(afterTrue)

        // Change value again and wait for it to complete
        repository.setHideTaggedWhenNoTagSelected(false)
        val afterFalse = repository.hideTaggedWhenNoTagSelected.first()
        values.add(afterFalse)

        // Verify all values were collected correctly
        assertEquals(3, values.size)
        assertFalse(values[0]) // Initial (set to false)
        assertTrue(values[1])  // After setting to true
        assertFalse(values[2]) // After setting to false
    }

    /**
     * Test that setting the same value twice works correctly
     */
    @Test
    fun setHideTaggedWhenNoTagSelected_sameValueTwice_works() = runTest {
        // Set to true
        repository.setHideTaggedWhenNoTagSelected(true)
        assertTrue(repository.hideTaggedWhenNoTagSelected.first())

        // Set to true again (idempotent operation)
        repository.setHideTaggedWhenNoTagSelected(true)
        assertTrue(repository.hideTaggedWhenNoTagSelected.first())
    }

    /**
     * Test that sequential writes work correctly
     */
    @Test
    fun setHideTaggedWhenNoTagSelected_sequentialWrites_lastWriteWins() = runTest {
        // Perform multiple sequential writes
        repository.setHideTaggedWhenNoTagSelected(true)

        // Verify intermediate state
        assertTrue(repository.hideTaggedWhenNoTagSelected.first())

        // Change to false
        repository.setHideTaggedWhenNoTagSelected(false)

        // Verify intermediate state
        assertFalse(repository.hideTaggedWhenNoTagSelected.first())

        // Change back to true
        repository.setHideTaggedWhenNoTagSelected(true)

        // The last write should win
        val finalValue = repository.hideTaggedWhenNoTagSelected.first()
        assertTrue(finalValue)
    }

    /**
     * Test reading value without prior writes in this test works
     */
    @Test
    fun settingsRepository_readCurrentValue_succeeds() = runTest {
        // This test just verifies we can read the current value
        // without any assertions about what it should be
        // (since DataStore may be shared between tests)
        val currentValue = repository.hideTaggedWhenNoTagSelected.first()

        // Verify we can toggle it
        repository.setHideTaggedWhenNoTagSelected(!currentValue)
        val newValue = repository.hideTaggedWhenNoTagSelected.first()

        // Verify it changed
        assertEquals(!currentValue, newValue)
    }
}
