package cat.company.qrreader.features.lock.presentation

import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.settings.GetAppLockEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAutoLockOnFocusLossUseCase
import cat.company.qrreader.domain.usecase.settings.SetAppLockEnabledUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AppLockViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppLockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSettingsRepository(
        appLockInitial: Boolean = false,
        autoLockInitial: Boolean = false
    ) : SettingsRepository {
        val appLockFlow = MutableStateFlow(appLockInitial)
        val autoLockFlow = MutableStateFlow(autoLockInitial)

        override val appLockEnabled: Flow<Boolean> = appLockFlow
        override suspend fun setAppLockEnabled(value: Boolean) { appLockFlow.value = value }
        override val autoLockOnFocusLoss: Flow<Boolean> = autoLockFlow
        override suspend fun setAutoLockOnFocusLoss(value: Boolean) { autoLockFlow.value = value }

        // Unused by AppLockViewModel – minimal stubs
        override val hideTaggedWhenNoTagSelected: Flow<Boolean> = MutableStateFlow(false)
        override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
        override val searchAcrossAllTagsWhenFiltering: Flow<Boolean> = MutableStateFlow(false)
        override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        override val aiGenerationEnabled: Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setAiGenerationEnabled(value: Boolean) {}
        override val aiLanguage: Flow<String> = MutableStateFlow("device")
        override suspend fun setAiLanguage(value: String) {}
        override val aiHumorousDescriptions: Flow<Boolean> = MutableStateFlow(false)
        override suspend fun setAiHumorousDescriptions(value: Boolean) {}
        override val showTagCounters: Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setShowTagCounters(value: Boolean) {}
        override val biometricLockEnabled: Flow<Boolean> = MutableStateFlow(false)
        override suspend fun setBiometricLockEnabled(value: Boolean) {}
        override val duplicateCheckEnabled: Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setDuplicateCheckEnabled(value: Boolean) {}
        override val hideLockedWhenNotInSafe: Flow<Boolean> = MutableStateFlow(false)
        override suspend fun setHideLockedWhenNotInSafe(value: Boolean) {}
    }

    private fun createViewModel(
        appLock: Boolean = false,
        autoLock: Boolean = false
    ): AppLockViewModel = createViewModelWithRepo(appLock, autoLock).first

    private fun createViewModelWithRepo(
        appLock: Boolean = false,
        autoLock: Boolean = false
    ): Pair<AppLockViewModel, FakeSettingsRepository> {
        val repo = FakeSettingsRepository(appLock, autoLock)
        val vm = AppLockViewModel(
            getAppLockEnabled = GetAppLockEnabledUseCase(repo),
            getAutoLockOnFocusLoss = GetAutoLockOnFocusLossUseCase(repo),
            setAppLockEnabled = SetAppLockEnabledUseCase(repo)
        )
        return vm to repo
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `isLocked is null before checkInitialLockState is called`() = runTest(testDispatcher) {
        val viewModel = createViewModel(appLock = true)
        assertNull(viewModel.isLocked.value)
    }

    // ── checkInitialLockState ────────────────────────────────────────────────

    @Test
    fun `checkInitialLockState sets locked true when appLock enabled and fresh launch`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(appLock = true)
            viewModel.checkInitialLockState(isRestoredInstance = false)
            advanceUntilIdle()

            assertTrue(viewModel.isLocked.value!!)
        }

    @Test
    fun `checkInitialLockState sets locked false when appLock disabled`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(appLock = false)
            viewModel.checkInitialLockState(isRestoredInstance = false)
            advanceUntilIdle()

            assertFalse(viewModel.isLocked.value!!)
        }

    @Test
    fun `checkInitialLockState keeps unlocked on restoration when already unlocked`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(appLock = true)
            // Simulate user unlocking
            viewModel.unlock()

            // Simulate Activity recreation (e.g. rotation)
            viewModel.checkInitialLockState(isRestoredInstance = true)
            advanceUntilIdle()

            assertFalse(viewModel.isLocked.value!!)
        }

    @Test
    fun `checkInitialLockState locks on restoration if still null (fresh ViewModel)`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(appLock = true)
            // isLocked is null (ViewModel was just created, not yet checked)
            viewModel.checkInitialLockState(isRestoredInstance = true)
            advanceUntilIdle()

            assertTrue(viewModel.isLocked.value!!)
        }

    // ── unlock ───────────────────────────────────────────────────────────────

    @Test
    fun `unlock sets isLocked to false`() = runTest(testDispatcher) {
        val viewModel = createViewModel(appLock = true)
        viewModel.checkInitialLockState(isRestoredInstance = false)
        advanceUntilIdle()

        viewModel.unlock()

        assertFalse(viewModel.isLocked.value!!)
    }

    // ── lockIfAutoLockEnabled ────────────────────────────────────────────────

    @Test
    fun `lockIfAutoLockEnabled locks when both appLock and autoLock are enabled`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(appLock = true, autoLock = true)
            viewModel.checkInitialLockState(isRestoredInstance = false)
            advanceUntilIdle()
            viewModel.unlock()

            viewModel.lockIfAutoLockEnabled()
            advanceUntilIdle()

            assertTrue(viewModel.isLocked.value!!)
        }

    @Test
    fun `lockIfAutoLockEnabled does not lock when autoLock is disabled`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(appLock = true, autoLock = false)
            viewModel.checkInitialLockState(isRestoredInstance = false)
            advanceUntilIdle()
            viewModel.unlock()

            viewModel.lockIfAutoLockEnabled()
            advanceUntilIdle()

            assertFalse(viewModel.isLocked.value!!)
        }

    @Test
    fun `lockIfAutoLockEnabled does not lock when appLock is disabled`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(appLock = false, autoLock = true)
            viewModel.checkInitialLockState(isRestoredInstance = false)
            advanceUntilIdle()

            viewModel.lockIfAutoLockEnabled()
            advanceUntilIdle()

            assertFalse(viewModel.isLocked.value!!)
        }

    // ── disableAndUnlock ─────────────────────────────────────────────────────

    @Test
    fun `disableAndUnlock unlocks and disables appLockEnabled setting`() =
        runTest(testDispatcher) {
            val (viewModel, repo) = createViewModelWithRepo(appLock = true)
            viewModel.checkInitialLockState(isRestoredInstance = false)
            advanceUntilIdle()

            viewModel.disableAndUnlock()
            advanceUntilIdle()

            assertFalse(viewModel.isLocked.value!!)
            assertFalse(repo.appLockFlow.value)
        }
}
