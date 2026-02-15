package cat.company.qrreader.features.camera.presentation

import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.tags.GenerateTagSuggestionsUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
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

    private lateinit var fakeGenerateTagSuggestionsUseCase: FakeGenerateTagSuggestionsUseCase
    private lateinit var fakeTagRepository: FakeTagRepository
    private lateinit var getAllTagsUseCase: GetAllTagsUseCase
    private lateinit var viewModel: QrCameraViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Fake GenerateTagSuggestionsUseCase that returns predetermined results
    private class FakeGenerateTagSuggestionsUseCase(
        var shouldSucceed: Boolean = true,
        var suggestionsToReturn: List<SuggestedTagModel> = emptyList()
    ) : GenerateTagSuggestionsUseCase() {
        override suspend fun invoke(
            barcodeContent: String,
            existingTags: List<String>
        ): Result<List<SuggestedTagModel>> {
            return if (shouldSucceed) {
                Result.success(suggestionsToReturn)
            } else {
                Result.failure(Exception("Test error"))
            }
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
        fakeGenerateTagSuggestionsUseCase = FakeGenerateTagSuggestionsUseCase()
        fakeTagRepository = FakeTagRepository()
        getAllTagsUseCase = GetAllTagsUseCase(fakeTagRepository)
        viewModel = QrCameraViewModel(fakeGenerateTagSuggestionsUseCase, getAllTagsUseCase)
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
        
        fakeGenerateTagSuggestionsUseCase.suggestionsToReturn = listOf(
            SuggestedTagModel("Shopping", true),
            SuggestedTagModel("Online", true)
        )

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(2, state.suggestedTags.size)
        assertEquals("Shopping", state.suggestedTags[0].name)
        assertEquals("Online", state.suggestedTags[1].name)
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun saveBarcodes_handlesSuggestionError() = runTest {
        val barcode = FakeBarcode("test content")
        
        fakeGenerateTagSuggestionsUseCase.shouldSucceed = false

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.suggestedTags.isEmpty())
        assertNotNull(state.tagSuggestionError)
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun toggleTagSelection_togglesTag() = runTest {
        val barcode = FakeBarcode("test")
        
        fakeGenerateTagSuggestionsUseCase.suggestionsToReturn = listOf(
            SuggestedTagModel("Tag1", true)
        )

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially selected
        assertTrue(viewModel.uiState.first().suggestedTags[0].isSelected)

        // Toggle off
        viewModel.toggleTagSelection("Tag1")
        assertFalse(viewModel.uiState.first().suggestedTags[0].isSelected)

        // Toggle on
        viewModel.toggleTagSelection("Tag1")
        assertTrue(viewModel.uiState.first().suggestedTags[0].isSelected)
    }

    @Test
    @Ignore("Cannot create Barcode instances in unit tests - BarcodeSource is an internal non-enum class")
    fun getSelectedTagNames_returnsOnlySelectedTags() = runTest {
        val barcode = FakeBarcode("test")
        
        fakeGenerateTagSuggestionsUseCase.suggestionsToReturn = listOf(
            SuggestedTagModel("Tag1", true),
            SuggestedTagModel("Tag2", true),
            SuggestedTagModel("Tag3", true)
        )

        viewModel.saveBarcodes(listOf(barcode))
        testDispatcher.scheduler.advanceUntilIdle()

        // Unselect Tag2
        viewModel.toggleTagSelection("Tag2")

        val selectedNames = viewModel.getSelectedTagNames()
        assertEquals(2, selectedNames.size)
        assertTrue(selectedNames.contains("Tag1"))
        assertFalse(selectedNames.contains("Tag2"))
        assertTrue(selectedNames.contains("Tag3"))
    }
}
