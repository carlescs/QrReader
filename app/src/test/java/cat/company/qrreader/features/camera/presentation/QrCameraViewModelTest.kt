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
import org.junit.Test
import org.robolectric.util.ReflectionHelpers

/**
 * Unit tests for QrCameraViewModel
 */
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
    ) : GenerateTagSuggestionsUseCase(
        context = ReflectionHelpers.createNullProxy(android.content.Context::class.java)
    ) {
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

        fun emitTags(tags: List<TagModel>) {
            tagsFlow.value = tags
        }
    }
    
    // Simple fake Barcode for testing
    private class FakeBarcode(val value: String) : Barcode() {
        init {
            // Use reflection to set the displayValue field
            try {
                val field = Barcode::class.java.getDeclaredField("displayValue")
                field.isAccessible = true
                field.set(this, value)
            } catch (e: Exception) {
                // Ignore if unable to set via reflection
            }
        }
        
        override fun getDisplayValue(): String = value
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
    fun saveBarcodes_updatesBarcodeState() {
        val barcode = FakeBarcode("test")
        val barcodes = listOf<Barcode>(barcode)

        viewModel.saveBarcodes(barcodes)

        assertEquals(barcodes, viewModel.uiState.value.lastBarcode)
    }

    @Test
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
