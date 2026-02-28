package cat.company.qrreader.domain.usecase.update

import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.UpdateAvailability
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class CheckAppUpdateUseCaseTest {

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMockManager(availability: Int): AppUpdateManager {
        val mockInfo = mock(AppUpdateInfo::class.java)
        `when`(mockInfo.updateAvailability()).thenReturn(availability)
        val mockManager = mock(AppUpdateManager::class.java)
        `when`(mockManager.appUpdateInfo).thenReturn(Tasks.forResult(mockInfo))
        return mockManager
    }

    @Test
    fun `invoke returns UpdateAvailable when Play Store has a newer version`() = runTest {
        val result = CheckAppUpdateUseCase(createMockManager(UpdateAvailability.UPDATE_AVAILABLE))()
        assertTrue(result is UpdateCheckResult.UpdateAvailable)
    }

    @Test
    fun `invoke returns UpdateAvailable for developer-triggered in-progress update`() = runTest {
        val result = CheckAppUpdateUseCase(
            createMockManager(UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
        )()
        assertTrue(result is UpdateCheckResult.UpdateAvailable)
    }

    @Test
    fun `invoke returns UpToDate when no update is available`() = runTest {
        val result = CheckAppUpdateUseCase(createMockManager(UpdateAvailability.UPDATE_NOT_AVAILABLE))()
        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun `invoke returns Error when app update info task fails`() = runTest {
        val result = CheckAppUpdateUseCase(FailingAppUpdateManager())()
        assertTrue(result is UpdateCheckResult.Error)
        assertEquals("Simulated network error", (result as UpdateCheckResult.Error).message)
    }
}
