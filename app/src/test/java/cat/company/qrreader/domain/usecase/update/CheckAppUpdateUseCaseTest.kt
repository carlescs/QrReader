package cat.company.qrreader.domain.usecase.update

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class CheckAppUpdateUseCaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns UpdateAvailable when Play Store has a newer version`() = runTest {
        val fakeManager = FakeAppUpdateManager(context)
        fakeManager.setUpdateAvailable(2)
        val result = CheckAppUpdateUseCase(fakeManager)()
        assertTrue(result is UpdateCheckResult.UpdateAvailable)
    }

    @Test
    fun `invoke returns UpToDate when no update is available`() = runTest {
        val fakeManager = FakeAppUpdateManager(context)
        // Default FakeAppUpdateManager state: no update available
        val result = CheckAppUpdateUseCase(fakeManager)()
        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun `invoke returns Error when app update info task fails`() = runTest {
        val result = CheckAppUpdateUseCase(FailingAppUpdateManager())()
        assertTrue(result is UpdateCheckResult.Error)
        assertEquals("Simulated network error", (result as UpdateCheckResult.Error).message)
    }
}
