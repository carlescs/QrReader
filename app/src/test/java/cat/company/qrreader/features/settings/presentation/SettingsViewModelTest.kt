package cat.company.qrreader.features.settings.presentation

import cat.company.qrreader.domain.model.BarcodeAiData
import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetBiometricLockEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetBiometricLockEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.GetShowTagCountersUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.SetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.SetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetShowTagCountersUseCase
import cat.company.qrreader.domain.usecase.update.CheckAppUpdateUseCase
import org.mockito.kotlin.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SettingsViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // ── Fakes ────────────────────────────────────────────────────────────────

    private class FakeSettingsRepository : SettingsRepository {
        val hideFlow = MutableStateFlow(false)
        val searchFlow = MutableStateFlow(false)
        val aiEnabledFlow = MutableStateFlow(true)
        val aiLanguageFlow = MutableStateFlow("en")
        val aiHumorousFlow = MutableStateFlow(false)
        val showTagCountersFlow = MutableStateFlow(true)

        override val hideTaggedWhenNoTagSelected: Flow<Boolean> = hideFlow
        override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) { hideFlow.value = value }
        override val searchAcrossAllTagsWhenFiltering: Flow<Boolean> = searchFlow
        override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) { searchFlow.value = value }
        override val aiGenerationEnabled: Flow<Boolean> = aiEnabledFlow
        override suspend fun setAiGenerationEnabled(value: Boolean) { aiEnabledFlow.value = value }
        override val aiLanguage: Flow<String> = aiLanguageFlow
        override suspend fun setAiLanguage(value: String) { aiLanguageFlow.value = value }
        override val aiHumorousDescriptions: Flow<Boolean> = aiHumorousFlow
        override suspend fun setAiHumorousDescriptions(value: Boolean) { aiHumorousFlow.value = value }
        override val showTagCounters: Flow<Boolean> = showTagCountersFlow
        override suspend fun setShowTagCounters(value: Boolean) { showTagCountersFlow.value = value }
        val biometricLockFlow = MutableStateFlow(false)
        override val biometricLockEnabled: Flow<Boolean> = biometricLockFlow
        override suspend fun setBiometricLockEnabled(value: Boolean) { biometricLockFlow.value = value }
    }

    private class FakeGenerateBarcodeAiDataUseCase(
        private val aiSupported: Boolean = false
    ) : GenerateBarcodeAiDataUseCase() {
        override suspend fun invoke(
            barcodeContent: String,
            barcodeType: String?,
            barcodeFormat: String?,
            existingTags: List<String>,
            language: String,
            humorous: Boolean,
            userTitle: String?,
            userDescription: String?
        ): Result<BarcodeAiData> = Result.success(
            BarcodeAiData(tags = emptyList(), description = "")
        )

        override suspend fun isAiSupportedOnDevice(): Boolean = aiSupported
        override suspend fun downloadModelIfNeeded() { /* no-op */ }
        override fun cleanup() { /* no-op */ }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private lateinit var fakeRepo: FakeSettingsRepository

    private fun createViewModel(aiSupported: Boolean = false): SettingsViewModel {
        fakeRepo = FakeSettingsRepository()
        return SettingsViewModel(
            historySettings = HistorySettingsUseCases(
                getHideTaggedSetting = GetHideTaggedSettingUseCase(fakeRepo),
                setHideTaggedSetting = SetHideTaggedSettingUseCase(fakeRepo),
                getSearchAcrossAllTags = GetSearchAcrossAllTagsUseCase(fakeRepo),
                setSearchAcrossAllTags = SetSearchAcrossAllTagsUseCase(fakeRepo),
                getShowTagCounters = GetShowTagCountersUseCase(fakeRepo),
                setShowTagCounters = SetShowTagCountersUseCase(fakeRepo),
                getBiometricLockEnabled = GetBiometricLockEnabledUseCase(fakeRepo),
                setBiometricLockEnabled = SetBiometricLockEnabledUseCase(fakeRepo)
            ),
            aiSettings = AiSettingsUseCases(
                getAiGenerationEnabled = GetAiGenerationEnabledUseCase(fakeRepo),
                setAiGenerationEnabled = SetAiGenerationEnabledUseCase(fakeRepo),
                getAiLanguage = GetAiLanguageUseCase(fakeRepo),
                setAiLanguage = SetAiLanguageUseCase(fakeRepo),
                getAiHumorousDescriptions = GetAiHumorousDescriptionsUseCase(fakeRepo),
                setAiHumorousDescriptions = SetAiHumorousDescriptionsUseCase(fakeRepo),
                generateBarcodeAiData = FakeGenerateBarcodeAiDataUseCase(aiSupported)
            ),
            checkAppUpdateUseCase = mock()
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── AI availability ──────────────────────────────────────────────────────

    @Test
    fun `isAiAvailableOnDevice is false when device does not support AI`() = runTest(testDispatcher) {
        val viewModel = createViewModel(aiSupported = false)
        advanceUntilIdle()

        assertFalse(viewModel.isAiAvailableOnDevice.value)
    }

    @Test
    fun `isAiAvailableOnDevice is true when device supports AI`() = runTest(testDispatcher) {
        val viewModel = createViewModel(aiSupported = true)
        advanceUntilIdle()

        assertTrue(viewModel.isAiAvailableOnDevice.value)
    }

    // ── Hide tagged setting ──────────────────────────────────────────────────

    @Test
    fun `hideTaggedWhenNoTagSelected emits initial value`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(false, viewModel.hideTaggedWhenNoTagSelected.first())
    }

    @Test
    fun `setHideTaggedWhenNoTagSelected updates flow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setHideTaggedWhenNoTagSelected(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.hideTaggedWhenNoTagSelected.first())
    }

    @Test
    fun `setHideTaggedWhenNoTagSelected toggle back to false`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setHideTaggedWhenNoTagSelected(true)
        advanceUntilIdle()
        viewModel.setHideTaggedWhenNoTagSelected(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.hideTaggedWhenNoTagSelected.first())
    }

    // ── Search across all tags setting ───────────────────────────────────────

    @Test
    fun `searchAcrossAllTagsWhenFiltering emits initial value`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(false, viewModel.searchAcrossAllTagsWhenFiltering.first())
    }

    @Test
    fun `setSearchAcrossAllTagsWhenFiltering updates flow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSearchAcrossAllTagsWhenFiltering(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.searchAcrossAllTagsWhenFiltering.first())
    }

    // ── AI generation enabled setting ────────────────────────────────────────

    @Test
    fun `aiGenerationEnabled emits initial value`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(true, viewModel.aiGenerationEnabled.first())
    }

    @Test
    fun `setAiGenerationEnabled updates flow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAiGenerationEnabled(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.aiGenerationEnabled.first())
    }

    @Test
    fun `setAiGenerationEnabled toggle back to true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAiGenerationEnabled(false)
        advanceUntilIdle()
        viewModel.setAiGenerationEnabled(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.aiGenerationEnabled.first())
    }

    // ── AI language setting ──────────────────────────────────────────────────

    @Test
    fun `aiLanguage emits initial value`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("en", viewModel.aiLanguage.first())
    }

    @Test
    fun `setAiLanguage updates flow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAiLanguage("es")
        advanceUntilIdle()

        assertEquals("es", viewModel.aiLanguage.first())
    }

    @Test
    fun `setAiLanguage to multiple values`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAiLanguage("fr")
        advanceUntilIdle()
        assertEquals("fr", viewModel.aiLanguage.first())

        viewModel.setAiLanguage("de")
        advanceUntilIdle()
        assertEquals("de", viewModel.aiLanguage.first())
    }

    // ── AI humorous descriptions setting ─────────────────────────────────────

    @Test
    fun `aiHumorousDescriptions emits initial value`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(false, viewModel.aiHumorousDescriptions.first())
    }

    @Test
    fun `setAiHumorousDescriptions updates flow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAiHumorousDescriptions(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.aiHumorousDescriptions.first())
    }

    @Test
    fun `setAiHumorousDescriptions toggle back to false`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAiHumorousDescriptions(true)
        advanceUntilIdle()
        viewModel.setAiHumorousDescriptions(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.aiHumorousDescriptions.first())
    }

    // ── Show tag counters setting ─────────────────────────────────────────────

    @Test
    fun `showTagCounters emits initial value true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(true, viewModel.showTagCounters.first())
    }

    @Test
    fun `setShowTagCounters updates flow to false`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setShowTagCounters(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.showTagCounters.first())
    }

    @Test
    fun `setShowTagCounters toggle back to true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setShowTagCounters(false)
        advanceUntilIdle()
        viewModel.setShowTagCounters(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.showTagCounters.first())
    }
}

