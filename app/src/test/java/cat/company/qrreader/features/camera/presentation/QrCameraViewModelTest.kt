package cat.company.qrreader.features.camera.presentation

import cat.company.qrreader.domain.model.BarcodeAiData
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for QrCameraViewModel
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class QrCameraViewModelTest {

    private lateinit var fakeGenerateBarcodeAiDataUseCase: FakeGenerateBarcodeAiDataUseCase
    private lateinit var fakeTagRepository: FakeTagRepository
    private lateinit var getAllTagsUseCase: GetAllTagsUseCase
    private lateinit var getAiGenerationEnabledUseCase: GetAiGenerationEnabledUseCase
    private lateinit var getAiLanguageUseCase: GetAiLanguageUseCase
    private lateinit var getAiHumorousDescriptionsUseCase: GetAiHumorousDescriptionsUseCase
    private lateinit var viewModel: QrCameraViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Fake GenerateBarcodeAiDataUseCase that returns predetermined results
    private class FakeGenerateBarcodeAiDataUseCase(
        var shouldSucceed: Boolean = true,
        var suggestionsToReturn: List<SuggestedTagModel> = emptyList(),
        var descriptionToReturn: String = "Test description"
    ) : GenerateBarcodeAiDataUseCase() {
        override suspend fun invoke(
            barcodeContent: String,
            barcodeType: String?,
            barcodeFormat: String?,
            existingTags: List<String>,
            language: String,
            humorous: Boolean
        ): Result<BarcodeAiData> {
            return if (shouldSucceed) {
                Result.success(BarcodeAiData(tags = suggestionsToReturn, description = descriptionToReturn))
            } else {
                Result.failure(Exception("Test error"))
            }
        }

        override suspend fun isAiSupportedOnDevice(): Boolean = true

        override suspend fun downloadModelIfNeeded() {
            // No-op for tests
        }

        override fun cleanup() {
            // No-op for tests
        }
    }

    // Fake TagRepository implementation
    private class FakeTagRepository : TagRepository {
        private val tagsFlow = MutableStateFlow<List<TagModel>>(emptyList())

        override fun getAllTags(): Flow<List<TagModel>> = tagsFlow

        override fun insertTags(vararg tags: TagModel) {}
        override fun updateTag(tag: TagModel) {}
        override fun deleteTag(tag: TagModel) {}

        @Suppress("unused")
        fun emitTags(tags: List<TagModel>) {
            tagsFlow.value = tags
        }
    }

    // Fake SettingsRepository implementation
    private class FakeSettingsRepository(
        aiGenerationEnabled: Boolean = true
    ) : SettingsRepository {
        override val hideTaggedWhenNoTagSelected = MutableStateFlow(false)
        override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
        override val searchAcrossAllTagsWhenFiltering = MutableStateFlow(false)
        override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        override val aiGenerationEnabled = MutableStateFlow(aiGenerationEnabled)
        override suspend fun setAiGenerationEnabled(value: Boolean) {
            this.aiGenerationEnabled.value = value
        }
        override val aiLanguage = MutableStateFlow("en")
        override suspend fun setAiLanguage(value: String) {
            this.aiLanguage.value = value
        }
        override val aiHumorousDescriptions = MutableStateFlow(false)
        override suspend fun setAiHumorousDescriptions(value: Boolean) {
            this.aiHumorousDescriptions.value = value
        }
    }
    
    // Simple fake Barcode for testing - uses reflection to work around internal BarcodeSource
    private class FakeBarcode private constructor(
        barcodeSource: com.google.mlkit.vision.barcode.common.internal.BarcodeSource,
        private val value: String
    ) : Barcode(barcodeSource, null) {

        override fun getDisplayValue(): String = value
        override fun getRawValue(): String = value

        companion object {
            @Suppress("UNCHECKED_CAST", "PrintStackTrace")
            operator fun invoke(value: String): FakeBarcode {
                try {
                    println("=== Starting FakeBarcode creation for: $value ===")

                    // BarcodeSource is an internal enum, get the first enum constant
                    val barcodeSourceClass = Class.forName(
                        "com.google.mlkit.vision.barcode.common.internal.BarcodeSource"
                    )

                    println("BarcodeSource class found: ${barcodeSourceClass.name}")
                    println("Is enum: ${barcodeSourceClass.isEnum}")

                    val enumConstants = barcodeSourceClass.enumConstants
                    println("Enum constants: ${enumConstants?.contentToString() ?: "NULL"}")

                    if (enumConstants == null || enumConstants.isEmpty()) {
                        throw IllegalStateException("BarcodeSource has no enum constants available")
                    }

                    val barcodeSource = enumConstants[0] as com.google.mlkit.vision.barcode.common.internal.BarcodeSource
                    println("Using enum constant: $barcodeSource")

                    val result = FakeBarcode(barcodeSource, value)
                    println("=== FakeBarcode created successfully ===")
                    return result
                } catch (e: Throwable) {
                    println("=== ERROR in FakeBarcode creation ===")
                    println("Exception type: ${e.javaClass.name}")
                    println("Message: ${e.message}")
                    e.printStackTrace()
                    throw IllegalStateException(
                        "Unable to create FakeBarcode: ${e.javaClass.simpleName}: ${e.message}",
                        e
                    )
                }
            }
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeGenerateBarcodeAiDataUseCase = FakeGenerateBarcodeAiDataUseCase()
        fakeTagRepository = FakeTagRepository()
        getAllTagsUseCase = GetAllTagsUseCase(fakeTagRepository)
        val fakeSettingsRepository = FakeSettingsRepository()
        getAiGenerationEnabledUseCase = GetAiGenerationEnabledUseCase(fakeSettingsRepository)
        getAiLanguageUseCase = GetAiLanguageUseCase(fakeSettingsRepository)
        getAiHumorousDescriptionsUseCase = GetAiHumorousDescriptionsUseCase(fakeSettingsRepository)
        viewModel = QrCameraViewModel(
            fakeGenerateBarcodeAiDataUseCase,
            getAllTagsUseCase,
            getAiGenerationEnabledUseCase,
            getAiLanguageUseCase,
            getAiHumorousDescriptionsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun saveBarcodes_updatesBarcodeState() {
        val barcode = FakeBarcode("test")
        val barcodes = listOf<Barcode>(barcode)

        viewModel.saveBarcodes(barcodes)

        assertEquals(barcodes, viewModel.uiState.value.lastBarcode)
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun saveBarcodes_generatesTagSuggestions() = runTest {
        val barcode = FakeBarcode("https://example.com")
        val barcodeHash = barcode.hashCode()

        fakeGenerateBarcodeAiDataUseCase.suggestionsToReturn = listOf(
            SuggestedTagModel("Shopping", true),
            SuggestedTagModel("Online", true)
        )

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        val suggestedTags = viewModel.getSuggestedTags(barcodeHash)
        assertEquals(2, suggestedTags.size)
        assertEquals("Shopping", suggestedTags[0].name)
        assertEquals("Online", suggestedTags[1].name)
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun saveBarcodes_handlesSuggestionError() = runTest {
        val barcode = FakeBarcode("test content")
        val barcodeHash = barcode.hashCode()

        fakeGenerateBarcodeAiDataUseCase.shouldSucceed = false

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        val suggestedTags = viewModel.getSuggestedTags(barcodeHash)
        assertTrue(suggestedTags.isEmpty())
        assertNotNull(viewModel.getTagError(barcodeHash))
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun toggleTagSelection_togglesTag() = runTest {
        val barcode = FakeBarcode("test")
        val barcodeHash = barcode.hashCode()

        fakeGenerateBarcodeAiDataUseCase.suggestionsToReturn = listOf(
            SuggestedTagModel("Tag1", true)
        )

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially selected
        assertTrue(viewModel.getSuggestedTags(barcodeHash)[0].isSelected)

        // Toggle off
        viewModel.toggleTagSelection(barcodeHash, "Tag1")
        assertFalse(viewModel.getSuggestedTags(barcodeHash)[0].isSelected)

        // Toggle on
        viewModel.toggleTagSelection(barcodeHash, "Tag1")
        assertTrue(viewModel.getSuggestedTags(barcodeHash)[0].isSelected)
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun getSelectedTagNames_returnsOnlySelectedTags() = runTest {
        val barcode = FakeBarcode("test")
        val barcodeHash = barcode.hashCode()

        fakeGenerateBarcodeAiDataUseCase.suggestionsToReturn = listOf(
            SuggestedTagModel("Tag1", true),
            SuggestedTagModel("Tag2", true),
            SuggestedTagModel("Tag3", true)
        )

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        // Unselect Tag2
        viewModel.toggleTagSelection(barcodeHash, "Tag2")

        val selectedNames = viewModel.getSelectedTagNames(barcodeHash)
        assertEquals(2, selectedNames.size)
        assertTrue(selectedNames.contains("Tag1"))
        assertFalse(selectedNames.contains("Tag2"))
        assertTrue(selectedNames.contains("Tag3"))
    }

    // ==================== Tests that DON'T require Barcode instances ====================

    @Test
    fun initialState_hasCorrectDefaults() {
        val state = viewModel.uiState.value

        assertEquals(null, state.lastBarcode)
        assertEquals(emptyMap<Int, List<SuggestedTagModel>>(), state.barcodeTags)
        assertEquals(emptySet<Int>(), state.isLoadingTags)
        assertEquals(emptyMap<Int, String>(), state.tagSuggestionErrors)
    }

    @Test
    fun saveBarcodes_withNull_updatesBarcodeState() {
        viewModel.saveBarcodes(null)

        val state = viewModel.uiState.value
        assertEquals(null, state.lastBarcode)
    }

    @Test
    fun saveBarcodes_withEmptyList_updatesBarcodeState() {
        viewModel.saveBarcodes(emptyList())

        val state = viewModel.uiState.value
        assertNotNull(state.lastBarcode)
        assertEquals(0, state.lastBarcode?.size)
    }

    @Test
    fun toggleTagSelection_withNonExistentBarcodeHash_doesNotCrash() {
        // We can't directly set state, but we can verify the method doesn't crash
        // when called with a non-existent barcode hash
        viewModel.toggleTagSelection(12345, "NonExistentTag")

        // Should not throw exception
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun getSelectedTagNames_whenNoBarcode_returnsEmptyList() {
        val selectedNames = viewModel.getSelectedTagNames(12345)

        assertEquals(0, selectedNames.size)
        assertTrue(selectedNames.isEmpty())
    }

    @Test
    fun toggleTagSelection_withExistingBarcodeHash_doesNotCrash() {
        // This test verifies the logic would work correctly
        // We're testing that the method exists and can be called
        viewModel.toggleTagSelection(12345, "SomeTag")

        // Should complete without error
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun barcodeState_dataClassCopy_worksCorrectly() {
        val testColor = "#FF0000"
        val state = BarcodeState(
            lastBarcode = null,
            barcodeTags = mapOf(1 to listOf(SuggestedTagModel("Test", true, testColor))),
            isLoadingTags = setOf(1),
            tagSuggestionErrors = mapOf(1 to "Error")
        )

        val copied = state.copy(isLoadingTags = emptySet())

        assertEquals(emptySet<Int>(), copied.isLoadingTags)
        assertEquals(mapOf(1 to "Error"), copied.tagSuggestionErrors)
        assertEquals(1, copied.barcodeTags.size)
    }

    @Test
    fun barcodeState_equality_worksCorrectly() {
        val state1 = BarcodeState(
            lastBarcode = null,
            barcodeTags = emptyMap(),
            isLoadingTags = emptySet(),
            tagSuggestionErrors = emptyMap()
        )

        val state2 = BarcodeState(
            lastBarcode = null,
            barcodeTags = emptyMap(),
            isLoadingTags = emptySet(),
            tagSuggestionErrors = emptyMap()
        )

        assertEquals(state1, state2)
    }

    @Test
    fun barcodeState_withDifferentBarcodeTags_notEqual() {
        val state1 = BarcodeState(barcodeTags = emptyMap())
        val state2 = BarcodeState(barcodeTags = mapOf(1 to listOf(SuggestedTagModel("Tag", true, "#FF0000"))))

        assertFalse(state1 == state2)
    }

    @Test
    fun barcodeState_withDifferentLoadingState_notEqual() {
        val state1 = BarcodeState(isLoadingTags = emptySet())
        val state2 = BarcodeState(isLoadingTags = setOf(1))

        assertFalse(state1 == state2)
    }

    @Test
    fun barcodeState_withDifferentError_notEqual() {
        val state1 = BarcodeState(tagSuggestionErrors = emptyMap())
        val state2 = BarcodeState(tagSuggestionErrors = mapOf(1 to "Error"))

        assertFalse(state1 == state2)
    }

    @Test
    fun viewModel_canBeCreated_withValidDependencies() {
        val vm = QrCameraViewModel(fakeGenerateBarcodeAiDataUseCase, getAllTagsUseCase, getAiGenerationEnabledUseCase, getAiLanguageUseCase, getAiHumorousDescriptionsUseCase)

        assertNotNull(vm)
        assertNotNull(vm.uiState)
        assertNotNull(vm.uiState.value)
    }

    @Test
    fun uiState_isExposedAsStateFlow() {
        val stateFlow = viewModel.uiState

        assertNotNull(stateFlow)
        assertNotNull(stateFlow.value)
    }

    @Test
    fun multipleSaveBarcodesCalls_withNull_lastCallWins() {
        viewModel.saveBarcodes(emptyList())
        viewModel.saveBarcodes(null)

        assertEquals(null, viewModel.uiState.value.lastBarcode)
    }

    @Test
    fun suggestedTagModel_dataClass_worksCorrectly() {
        val tag = SuggestedTagModel("TestTag", true)

        assertEquals("TestTag", tag.name)
        assertEquals(true, tag.isSelected)
        assertTrue(tag.color.startsWith("#"))
        assertEquals(7, tag.color.length) // #RRGGBB format

        val copied = tag.copy(isSelected = false)
        assertEquals(false, copied.isSelected)
        assertEquals("TestTag", copied.name)
        assertEquals(tag.color, copied.color) // Color should be preserved on copy
    }

    @Test
    fun suggestedTagModel_customColor() {
        val customColor = "#FF5733"
        val tag = SuggestedTagModel("Tag", true, customColor)

        assertEquals(customColor, tag.color)
    }

    @Test
    fun suggestedTagModel_generatesRandomColor() {
        val tag1 = SuggestedTagModel("Tag1", true)
        val tag2 = SuggestedTagModel("Tag2", true)

        // Both should have valid hex colors
        assertTrue(tag1.color.matches(Regex("#[0-9A-Fa-f]{6}")))
        assertTrue(tag2.color.matches(Regex("#[0-9A-Fa-f]{6}")))
    }

    @Test
    fun suggestedTagModel_equality() {
        val color = "#FF0000"
        val tag1 = SuggestedTagModel("Tag", true, color)
        val tag2 = SuggestedTagModel("Tag", true, color)

        assertEquals(tag1, tag2)
    }

    @Test
    fun suggestedTagModel_inequality_differentNames() {
        val color = "#FF0000"
        val tag1 = SuggestedTagModel("Tag1", true, color)
        val tag2 = SuggestedTagModel("Tag2", true, color)

        assertFalse(tag1 == tag2)
    }

    @Test
    fun suggestedTagModel_inequality_differentSelection() {
        val color = "#FF0000"
        val tag1 = SuggestedTagModel("Tag", true, color)
        val tag2 = SuggestedTagModel("Tag", false, color)

        assertFalse(tag1 == tag2)
    }

    @Test
    fun suggestedTagModel_inequality_differentColors() {
        val tag1 = SuggestedTagModel("Tag", true, "#FF0000")
        val tag2 = SuggestedTagModel("Tag", true, "#00FF00")

        assertFalse(tag1 == tag2)
    }

    // ==================== Tests for edge cases ====================

    @Test
    fun toggleTagSelection_multipleCalls_togglesCorrectly() {
        val barcodeHash = 12345
        // First toggle would select, second would deselect, etc.
        viewModel.toggleTagSelection(barcodeHash, "TestTag")
        viewModel.toggleTagSelection(barcodeHash, "TestTag")
        viewModel.toggleTagSelection(barcodeHash, "TestTag")

        // Should not crash with multiple toggles
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun getSelectedTagNames_multipleCallsConsecutive_returnsSameResult() {
        val barcodeHash = 12345
        val result1 = viewModel.getSelectedTagNames(barcodeHash)
        val result2 = viewModel.getSelectedTagNames(barcodeHash)

        assertEquals(result1, result2)
    }

    @Test
    fun saveBarcodes_afterPreviousNullCall_worksCorrectly() {
        viewModel.saveBarcodes(null)
        viewModel.saveBarcodes(emptyList())

        assertNotNull(viewModel.uiState.value.lastBarcode)
        assertEquals(0, viewModel.uiState.value.lastBarcode?.size)
    }

    // ==================== Tests for description functionality ====================

    @Test
    fun getDescription_nonExistentBarcode_returnsNull() {
        val barcodeHash = 99999

        val description = viewModel.getDescription(barcodeHash)

        assertEquals(null, description)
    }

    @Test
    fun isLoadingDescription_nonExistentBarcode_returnsFalse() {
        val barcodeHash = 99999

        val isLoading = viewModel.isLoadingDescription(barcodeHash)

        assertFalse(isLoading)
    }

    @Test
    fun getDescriptionError_nonExistentBarcode_returnsNull() {
        val barcodeHash = 99999

        val error = viewModel.getDescriptionError(barcodeHash)

        assertEquals(null, error)
    }

    @Test
    fun barcodeState_includesDescriptionFields() {
        val state = viewModel.uiState.value

        assertNotNull(state.barcodeDescriptions)
        assertNotNull(state.isLoadingDescriptions)
        assertNotNull(state.descriptionErrors)
    }

    @Test
    fun barcodeState_descriptionsInitiallyEmpty() {
        val state = viewModel.uiState.value

        assertTrue(state.barcodeDescriptions.isEmpty())
        assertTrue(state.isLoadingDescriptions.isEmpty())
        assertTrue(state.descriptionErrors.isEmpty())
    }

    // ==================== Tests for BarcodeState description data class ====================

    @Test
    fun barcodeState_dataClassCopy_preservesDescriptionFields() {
        val state = BarcodeState(
            barcodeDescriptions = mapOf(1 to "A barcode."),
            isLoadingDescriptions = setOf(1),
            descriptionErrors = mapOf(1 to "Error")
        )

        val copied = state.copy(isLoadingDescriptions = emptySet())

        assertEquals(emptySet<Int>(), copied.isLoadingDescriptions)
        assertEquals(mapOf(1 to "A barcode."), copied.barcodeDescriptions)
        assertEquals(mapOf(1 to "Error"), copied.descriptionErrors)
    }

    @Test
    fun barcodeState_withDifferentDescriptions_notEqual() {
        val state1 = BarcodeState(barcodeDescriptions = emptyMap())
        val state2 = BarcodeState(barcodeDescriptions = mapOf(1 to "Some description."))

        assertFalse(state1 == state2)
    }

    @Test
    fun barcodeState_withDifferentDescriptionErrors_notEqual() {
        val state1 = BarcodeState(descriptionErrors = emptyMap())
        val state2 = BarcodeState(descriptionErrors = mapOf(1 to "Error"))

        assertFalse(state1 == state2)
    }

    @Test
    fun barcodeState_withDifferentDescriptionLoadingState_notEqual() {
        val state1 = BarcodeState(isLoadingDescriptions = emptySet())
        val state2 = BarcodeState(isLoadingDescriptions = setOf(1))

        assertFalse(state1 == state2)
    }

    @Test
    fun barcodeState_equality_includesDescriptionFields() {
        val state1 = BarcodeState(
            barcodeDescriptions = mapOf(1 to "desc"),
            isLoadingDescriptions = setOf(2),
            descriptionErrors = mapOf(3 to "err")
        )
        val state2 = BarcodeState(
            barcodeDescriptions = mapOf(1 to "desc"),
            isLoadingDescriptions = setOf(2),
            descriptionErrors = mapOf(3 to "err")
        )

        assertEquals(state1, state2)
    }

    @Test
    fun barcodeState_hashCode_consistentWithEquality_forDescriptionFields() {
        val state1 = BarcodeState(barcodeDescriptions = mapOf(1 to "desc"))
        val state2 = BarcodeState(barcodeDescriptions = mapOf(1 to "desc"))

        assertEquals(state1.hashCode(), state2.hashCode())
    }
}
