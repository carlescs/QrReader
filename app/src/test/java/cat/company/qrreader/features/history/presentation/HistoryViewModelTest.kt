package cat.company.qrreader.features.history.presentation

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.ToggleFavoriteUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Minimal fake implementation of BarcodeRepository for unit tests
    private class FakeBarcodeRepository : BarcodeRepository {
        // Expose a mutable flow so tests can emit values
        private val resultFlow = MutableStateFlow<List<BarcodeWithTagsModel>>(emptyList())
        // Record the last parameters received
        var lastRequest: Triple<Int?, String?, Boolean?>? = null

        override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())

        override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = resultFlow

        override fun getBarcodesWithTagsByFilter(
            tagId: Int?,
            query: String?,
            hideTaggedWhenNoTagSelected: Boolean,
            searchAcrossAllTagsWhenFiltering: Boolean,
            showOnlyFavorites: Boolean
        ): Flow<List<BarcodeWithTagsModel>> {
            lastRequest = Triple(tagId, query, hideTaggedWhenNoTagSelected)
            return resultFlow
        }

        override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}

        override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L

        override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0

        override suspend fun deleteBarcode(barcode: BarcodeModel) {}

        override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}

        override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}

        override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}

        override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}

        override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
        override fun getFavoritesCount(): Flow<Int> = flowOf(0)

        fun emitResult(list: List<BarcodeWithTagsModel>) {
            resultFlow.value = list
        }
    }

    private class FakeGenerateBarcodeAiDataUseCase : GenerateBarcodeAiDataUseCase() {
        override suspend fun invoke(
            barcodeContent: String,
            barcodeType: String?,
            barcodeFormat: String?,
            existingTags: List<String>,
            language: String,
            humorous: Boolean,
            userTitle: String?,
            userDescription: String?
        ) = Result.failure<cat.company.qrreader.domain.model.BarcodeAiData>(
            UnsupportedOperationException("AI not available in tests")
        )
        override suspend fun isAiSupportedOnDevice(): Boolean = true
        override suspend fun downloadModelIfNeeded() {}
        override fun cleanup() {}
    }

    private fun makeFakeSettingsRepo() = object : cat.company.qrreader.domain.repository.SettingsRepository {
        override val hideTaggedWhenNoTagSelected: Flow<Boolean>
            get() = flowOf(false)
        override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
        override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
            get() = flowOf(false)
        override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        override val aiGenerationEnabled: Flow<Boolean>
            get() = flowOf(true)
        override suspend fun setAiGenerationEnabled(value: Boolean) {}
        override val aiLanguage: Flow<String>
            get() = flowOf("en")
        override suspend fun setAiLanguage(value: String) {}
        override val aiHumorousDescriptions: Flow<Boolean>
            get() = flowOf(false)
        override suspend fun setAiHumorousDescriptions(value: Boolean) {}
        override val showTagCounters: Flow<Boolean>
            get() = flowOf(true)
        override suspend fun setShowTagCounters(value: Boolean) {}
    }

    private fun makeViewModel(repository: FakeBarcodeRepository): HistoryViewModel {
        val fakeSettingsRepo = makeFakeSettingsRepo()
        return HistoryViewModel(
            GetBarcodesWithTagsUseCase(repository),
            UpdateBarcodeUseCase(repository),
            DeleteBarcodeUseCase(repository),
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(repository)
        )
    }

    @Test
    fun onQueryChange_updatesState() {
        val vm = makeViewModel(FakeBarcodeRepository())
        vm.onQueryChange("abc")
        assertEquals("abc", vm.searchQuery.value)
    }

    @Test
    fun onTagSelected_updatesState() {
        val vm = makeViewModel(FakeBarcodeRepository())
        vm.onTagSelected(42)
        assertEquals(42, vm.selectedTagId.value)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun savedBarcodes_respectsDebounce_and_emitsResults() = runTest {
        val fakeRepository = FakeBarcodeRepository()
        val vm = makeViewModel(fakeRepository)

        val collected = mutableListOf<List<BarcodeWithTagsModel>>()
        val job = launch { vm.savedBarcodes.collect { collected.add(it) } }

        // Change query and tag
        vm.onTagSelected(7)
        vm.onQueryChange("  abc  ") // will be trimmed to "abc"

        // Advance virtual time by debounce interval (250ms) so the ViewModel queries the repository
        advanceTimeBy(250)
        // Run pending tasks produced by the advance
        runCurrent()

        // Now repository should have been called with trimmed query
        // When searchAcrossAllTagsWhenFiltering=false, tagId is preserved even with a query
        assertEquals(7, fakeRepository.lastRequest?.first)
        assertEquals("abc", fakeRepository.lastRequest?.second)

        // Emit a sample result and ensure it's received
        val sampleBarcode = BarcodeModel(id = 1, type = 1, format = 1, barcode = "X", date = Date())
        val sample = listOf(BarcodeWithTagsModel(sampleBarcode, emptyList()))
        fakeRepository.emitResult(sample)

        // Allow the dispatcher to run pending tasks
        advanceTimeBy(1)
        runCurrent()

        // There should be at least one emission (initial empty plus the new sample)
        // The last collected value should be the sample
        assertEquals(sample, collected.last())

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchIgnoresSelectedTag_whenSettingEnabled_andQueryNonEmpty() = runTest {
        val fakeRepository = FakeBarcodeRepository()

        // Settings: enable search across all tags
        val settingsHideFlag = MutableStateFlow(false)
        val settingsSearchAcrossAll = MutableStateFlow(true)

        val fakeSettingsRepository = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = settingsHideFlag

            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {
                settingsHideFlag.value = value
            }

            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = settingsSearchAcrossAll

            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {
                settingsSearchAcrossAll.value = value
            }
            override val aiGenerationEnabled: Flow<Boolean>
                get() = flowOf(true)
            override suspend fun setAiGenerationEnabled(value: Boolean) {}
            override val aiLanguage: Flow<String>
                get() = flowOf("en")
            override suspend fun setAiLanguage(value: String) {}
            override val aiHumorousDescriptions: Flow<Boolean>
                get() = flowOf(false)
            override suspend fun setAiHumorousDescriptions(value: Boolean) {}
            override val showTagCounters: Flow<Boolean>
                get() = flowOf(true)
            override suspend fun setShowTagCounters(value: Boolean) {}
        }

        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(fakeRepository),
            UpdateBarcodeUseCase(fakeRepository),
            DeleteBarcodeUseCase(fakeRepository),
            fakeSettingsRepository,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepository),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepository),
            ToggleFavoriteUseCase(fakeRepository)
        )

        val collected = mutableListOf<List<BarcodeWithTagsModel>>()
        val job = launch { vm.savedBarcodes.collect { collected.add(it) } }

        // Select a tag, then type a query
        vm.onTagSelected(77)
        vm.onQueryChange("searchme")

        // Advance by debounce interval
        advanceTimeBy(250)
        runCurrent()

        // The repository should have been called with tagId = null because searchAcrossAllTagsWhenFiltering=true
        assertEquals(null, fakeRepository.lastRequest?.first)
        assertEquals("searchme", fakeRepository.lastRequest?.second)

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun regenerateAiDescription_onSuccess_updatesDescriptionState() = runTest {
        val successDescription = "A QR code pointing to example.com."
        val fakeAiUseCase = object : GenerateBarcodeAiDataUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String,
                humorous: Boolean,
                userTitle: String?,
                userDescription: String?
            ) = Result.success(
                cat.company.qrreader.domain.model.BarcodeAiData(
                    tags = emptyList(),
                    description = successDescription
                )
            )
            override suspend fun isAiSupportedOnDevice(): Boolean = true
            override suspend fun downloadModelIfNeeded() {}
            override fun cleanup() {}
        }
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            fakeAiUseCase,
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(id = 1, type = 4, format = 256, barcode = "https://example.com", date = Date())
        vm.regenerateAiDescription(barcode)
        advanceUntilIdle()

        assertEquals(successDescription, vm.regenerateDescriptionState.value.description)
        assertEquals(null, vm.regenerateDescriptionState.value.error)
        assertEquals(false, vm.regenerateDescriptionState.value.isLoading)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun regenerateAiDescription_onFailure_updatesErrorState() = runTest {
        val errorMessage = "AI not available on this device"
        val fakeAiUseCase = object : GenerateBarcodeAiDataUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String,
                humorous: Boolean,
                userTitle: String?,
                userDescription: String?
            ) = Result.failure<cat.company.qrreader.domain.model.BarcodeAiData>(
                UnsupportedOperationException(errorMessage)
            )
            override suspend fun isAiSupportedOnDevice(): Boolean = true
            override suspend fun downloadModelIfNeeded() {}
            override fun cleanup() {}
        }
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            fakeAiUseCase,
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(id = 1, type = 4, format = 256, barcode = "https://example.com", date = Date())
        vm.regenerateAiDescription(barcode)
        advanceUntilIdle()

        assertEquals(null, vm.regenerateDescriptionState.value.description)
        assertEquals(errorMessage, vm.regenerateDescriptionState.value.error)
        assertEquals(false, vm.regenerateDescriptionState.value.isLoading)
    }

    @Test
    fun resetRegenerateDescriptionState_clearsState() = runTest {
        val errorMessage = "AI not available on this device"
        val fakeAiUseCase = object : GenerateBarcodeAiDataUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String,
                humorous: Boolean,
                userTitle: String?,
                userDescription: String?
            ) = Result.failure<cat.company.qrreader.domain.model.BarcodeAiData>(
                UnsupportedOperationException(errorMessage)
            )
            override suspend fun isAiSupportedOnDevice(): Boolean = true
            override suspend fun downloadModelIfNeeded() {}
            override fun cleanup() {}
        }
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            fakeAiUseCase,
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        // Trigger a failure to put the ViewModel in an error state
        val barcode = BarcodeModel(id = 1, type = 4, format = 256, barcode = "https://example.com", date = Date())
        vm.regenerateAiDescription(barcode)
        advanceUntilIdle()
        assertEquals(errorMessage, vm.regenerateDescriptionState.value.error)

        // Reset should clear all state
        vm.resetRegenerateDescriptionState()
        assertEquals(HistoryViewModel.RegenerateDescriptionState(), vm.regenerateDescriptionState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun toggleFavoritesFilter_changesSavedBarcodesRequest() = runTest {
        // Repository that records the showOnlyFavorites parameter
        var lastShowOnlyFavorites = false
        val repo = object : BarcodeRepository {
            private val resultFlow = MutableStateFlow<List<BarcodeWithTagsModel>>(emptyList())
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = resultFlow
            override fun getBarcodesWithTagsByFilter(
                tagId: Int?, query: String?,
                hideTaggedWhenNoTagSelected: Boolean,
                searchAcrossAllTagsWhenFiltering: Boolean,
                showOnlyFavorites: Boolean
            ): Flow<List<BarcodeWithTagsModel>> {
                lastShowOnlyFavorites = showOnlyFavorites
                return resultFlow
            }
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
            override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
            override fun getFavoritesCount(): Flow<Int> = flowOf(0)
        }
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(repo),
            UpdateBarcodeUseCase(repo),
            DeleteBarcodeUseCase(repo),
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(repo)
        )

        val job = launch { vm.savedBarcodes.collect { } }

        // Initially, showOnlyFavorites should be false
        advanceTimeBy(250)
        runCurrent()
        assertEquals(false, lastShowOnlyFavorites)
        assertEquals(false, vm.showOnlyFavorites.value)

        // Toggle favorites filter on
        vm.toggleFavoritesFilter()
        advanceTimeBy(250)
        runCurrent()
        assertEquals(true, lastShowOnlyFavorites)
        assertEquals(true, vm.showOnlyFavorites.value)

        // Toggle favorites filter off again
        vm.toggleFavoritesFilter()
        advanceTimeBy(250)
        runCurrent()
        assertEquals(false, lastShowOnlyFavorites)
        assertEquals(false, vm.showOnlyFavorites.value)

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun favoritesFilter_overridesTagAndSearchFilters() = runTest {
        // Repository that records the last call parameters
        var lastTagId: Int? = -1 // use -1 as sentinel "not yet called"
        var lastQuery: String? = "sentinel"
        var lastShowOnlyFavorites = false
        val repo = object : BarcodeRepository {
            private val resultFlow = MutableStateFlow<List<BarcodeWithTagsModel>>(emptyList())
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = resultFlow
            override fun getBarcodesWithTagsByFilter(
                tagId: Int?, query: String?,
                hideTaggedWhenNoTagSelected: Boolean,
                searchAcrossAllTagsWhenFiltering: Boolean,
                showOnlyFavorites: Boolean
            ): Flow<List<BarcodeWithTagsModel>> {
                lastTagId = tagId
                lastQuery = query
                lastShowOnlyFavorites = showOnlyFavorites
                return resultFlow
            }
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
            override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
            override fun getFavoritesCount(): Flow<Int> = flowOf(0)
        }
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(repo),
            UpdateBarcodeUseCase(repo),
            DeleteBarcodeUseCase(repo),
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(repo)
        )

        val job = launch { vm.savedBarcodes.collect { } }

        // Set a tag and a search query
        vm.onTagSelected(5)
        vm.onQueryChange("hello")
        advanceTimeBy(250)
        runCurrent()

        // Sanity check: tag and query are forwarded normally without favorites
        assertEquals(5, lastTagId)
        assertEquals("hello", lastQuery)
        assertEquals(false, lastShowOnlyFavorites)

        // Enable favorites filter — must ignore tag and search
        vm.toggleFavoritesFilter()
        advanceTimeBy(250)
        runCurrent()

        assertEquals(null, lastTagId)
        assertEquals(null, lastQuery)
        assertEquals(true, lastShowOnlyFavorites)

        job.cancel()
    }

    @Test
    fun toggleFavoritesFilter_clearSelectedTag() {
        val vm = makeViewModel(FakeBarcodeRepository())
        vm.onTagSelected(3)
        assertEquals(3, vm.selectedTagId.value)

        vm.toggleFavoritesFilter()
        assertEquals(true, vm.showOnlyFavorites.value)
        assertEquals(null, vm.selectedTagId.value)
    }

    @Test
    fun toggleFavoritesFilter_off_doesNotRestoreTag() {
        val vm = makeViewModel(FakeBarcodeRepository())
        vm.onTagSelected(3)
        vm.toggleFavoritesFilter() // ON — clears tag
        vm.toggleFavoritesFilter() // OFF — tag stays null
        assertEquals(false, vm.showOnlyFavorites.value)
        assertEquals(null, vm.selectedTagId.value)
    }

    @Test
    fun onTagSelected_clearsFavoritesFilter() {
        val vm = makeViewModel(FakeBarcodeRepository())
        vm.toggleFavoritesFilter()
        assertEquals(true, vm.showOnlyFavorites.value)

        vm.onTagSelected(7)
        assertEquals(7, vm.selectedTagId.value)
        assertEquals(false, vm.showOnlyFavorites.value)
    }

    @Test
    fun onTagSelected_null_clearsFavoritesFilter() {
        val vm = makeViewModel(FakeBarcodeRepository())
        vm.toggleFavoritesFilter()
        assertEquals(true, vm.showOnlyFavorites.value)

        vm.onTagSelected(null)
        assertEquals(null, vm.selectedTagId.value)
        assertEquals(false, vm.showOnlyFavorites.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun toggleFavorite_callsUseCaseWithCorrectArguments() = runTest {
        var lastBarcodeId: Int? = null
        var lastIsFavorite: Boolean? = null
        val repo = object : BarcodeRepository {
            private val resultFlow = MutableStateFlow<List<BarcodeWithTagsModel>>(emptyList())
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = resultFlow
            override fun getBarcodesWithTagsByFilter(
                tagId: Int?, query: String?,
                hideTaggedWhenNoTagSelected: Boolean,
                searchAcrossAllTagsWhenFiltering: Boolean,
                showOnlyFavorites: Boolean
            ): Flow<List<BarcodeWithTagsModel>> = resultFlow
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {
                lastBarcodeId = barcodeId
                lastIsFavorite = isFavorite
            }
            override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
            override fun getFavoritesCount(): Flow<Int> = flowOf(0)
        }
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(repo),
            UpdateBarcodeUseCase(repo),
            DeleteBarcodeUseCase(repo),
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(repo)
        )

        // Mark barcode 42 as favorite
        vm.toggleFavorite(42, true)
        advanceUntilIdle()

        assertEquals(42, lastBarcodeId)
        assertEquals(true, lastIsFavorite)

        // Remove favorite from barcode 7
        vm.toggleFavorite(7, false)
        advanceUntilIdle()

        assertEquals(7, lastBarcodeId)
        assertEquals(false, lastIsFavorite)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun suggestTags_onAiFailure_setsError() = runTest {
        val errorMessage = "AI not available in tests"
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(id = 5, type = 1, format = 1, barcode = "https://example.com", date = Date())
        val barcodeWithTags = BarcodeWithTagsModel(barcode, emptyList())

        vm.suggestTags(barcodeWithTags, emptyList())
        advanceUntilIdle()

        val state = vm.tagSuggestionStates.value[5]
        assertEquals(false, state?.isLoading)
        assertEquals(errorMessage, state?.error)
        assertEquals(true, state?.suggestedTags?.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun suggestTags_setsLoadingWhileInProgress() = runTest {
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(id = 7, type = 1, format = 1, barcode = "test", date = Date())
        val barcodeWithTags = BarcodeWithTagsModel(barcode, emptyList())

        // Before calling, state should be absent (no entry for this barcode)
        assertEquals(null, vm.tagSuggestionStates.value[7])

        vm.suggestTags(barcodeWithTags, emptyList())

        // isLoading is set synchronously before the coroutine launches, so it is
        // immediately visible without needing to advance the test dispatcher.
        assertEquals(true, vm.tagSuggestionStates.value[7]?.isLoading)

        // After completing, loading should be false
        advanceUntilIdle()
        assertEquals(false, vm.tagSuggestionStates.value[7]?.isLoading)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun suggestTags_onSuccess_returnsSuggestedTags() = runTest {
        val fakeSettingsRepo = makeFakeSettingsRepo()

        val successAiUseCase = object : GenerateBarcodeAiDataUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String,
                humorous: Boolean,
                userTitle: String?,
                userDescription: String?
            ) = Result.success(
                cat.company.qrreader.domain.model.BarcodeAiData(
                    tags = listOf(
                        cat.company.qrreader.domain.model.SuggestedTagModel(name = "Shopping", isSelected = true),
                        cat.company.qrreader.domain.model.SuggestedTagModel(name = "Online", isSelected = true)
                    ),
                    description = "A test description"
                )
            )
            override suspend fun isAiSupportedOnDevice(): Boolean = true
            override suspend fun downloadModelIfNeeded() {}
            override fun cleanup() {}
        }

        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            successAiUseCase,
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(id = 9, type = 1, format = 1, barcode = "https://shop.example.com", date = Date())
        val barcodeWithTags = BarcodeWithTagsModel(barcode, emptyList())

        vm.suggestTags(barcodeWithTags, listOf("Shopping", "Work"))
        advanceUntilIdle()

        val state = vm.tagSuggestionStates.value[9]
        assertEquals(false, state?.isLoading)
        assertEquals(null, state?.error)
        // Tags should be returned with isSelected = false (unselected by default in history)
        assertEquals(2, state?.suggestedTags?.size)
        assertEquals("Shopping", state?.suggestedTags?.get(0)?.name)
        assertEquals(false, state?.suggestedTags?.get(0)?.isSelected)
        assertEquals("Online", state?.suggestedTags?.get(1)?.name)
        assertEquals(false, state?.suggestedTags?.get(1)?.isSelected)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resetTagSuggestionState_clearsStateForBarcode() = runTest {
        val fakeSettingsRepo = makeFakeSettingsRepo()
        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(id = 11, type = 1, format = 1, barcode = "test", date = Date())
        val barcodeWithTags = BarcodeWithTagsModel(barcode, emptyList())

        // Generate suggestions (will fail, which is fine — we just want state present)
        vm.suggestTags(barcodeWithTags, emptyList())
        advanceUntilIdle()

        // State entry should exist
        assertEquals(true, vm.tagSuggestionStates.value.containsKey(11))

        // Reset should remove the entry
        vm.resetTagSuggestionState(11)
        assertEquals(false, vm.tagSuggestionStates.value.containsKey(11))
    }

    // -------------------------------------------------------------------
    // Tests verifying that userTitle/userDescription are forwarded
    // -------------------------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun regenerateAiDescription_forwardsTitleAndDescription_toUseCase() = runTest {
        val fakeSettingsRepo = makeFakeSettingsRepo()
        var capturedTitle: String? = "NOT_SET"
        var capturedDescription: String? = "NOT_SET"

        val capturingUseCase = object : GenerateBarcodeAiDataUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String,
                humorous: Boolean,
                userTitle: String?,
                userDescription: String?
            ): Result<cat.company.qrreader.domain.model.BarcodeAiData> {
                capturedTitle = userTitle
                capturedDescription = userDescription
                return Result.success(
                    cat.company.qrreader.domain.model.BarcodeAiData(tags = emptyList(), description = "ok")
                )
            }
            override suspend fun isAiSupportedOnDevice(): Boolean = true
            override suspend fun downloadModelIfNeeded() {}
            override fun cleanup() {}
        }

        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            capturingUseCase,
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(
            id = 1, type = 4, format = 256, barcode = "https://example.com", date = Date(),
            title = "My Link", description = "A useful website"
        )
        vm.regenerateAiDescription(barcode)
        advanceUntilIdle()

        assertEquals("My Link", capturedTitle)
        assertEquals("A useful website", capturedDescription)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun suggestTags_forwardsTitleAndDescription_toUseCase() = runTest {
        val fakeSettingsRepo = makeFakeSettingsRepo()
        var capturedTitle: String? = "NOT_SET"
        var capturedDescription: String? = "NOT_SET"

        val capturingUseCase = object : GenerateBarcodeAiDataUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String,
                humorous: Boolean,
                userTitle: String?,
                userDescription: String?
            ): Result<cat.company.qrreader.domain.model.BarcodeAiData> {
                capturedTitle = userTitle
                capturedDescription = userDescription
                return Result.success(
                    cat.company.qrreader.domain.model.BarcodeAiData(tags = emptyList(), description = "ok")
                )
            }
            override suspend fun isAiSupportedOnDevice(): Boolean = true
            override suspend fun downloadModelIfNeeded() {}
            override fun cleanup() {}
        }

        val vm = HistoryViewModel(
            GetBarcodesWithTagsUseCase(FakeBarcodeRepository()),
            UpdateBarcodeUseCase(FakeBarcodeRepository()),
            DeleteBarcodeUseCase(FakeBarcodeRepository()),
            fakeSettingsRepo,
            capturingUseCase,
            GetAiLanguageUseCase(fakeSettingsRepo),
            GetAiHumorousDescriptionsUseCase(fakeSettingsRepo),
            ToggleFavoriteUseCase(FakeBarcodeRepository())
        )

        val barcode = BarcodeModel(
            id = 5, type = 1, format = 1, barcode = "https://shop.example.com", date = Date(),
            title = "Coffee Shop", description = "Tuesday loyalty card"
        )
        val barcodeWithTags = BarcodeWithTagsModel(barcode, emptyList())
        vm.suggestTags(barcodeWithTags, emptyList())
        advanceUntilIdle()

        assertEquals("Coffee Shop", capturedTitle)
        assertEquals("Tuesday loyalty card", capturedDescription)
    }
}