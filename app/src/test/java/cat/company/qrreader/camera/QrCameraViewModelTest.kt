package cat.company.qrreader.camera

import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.tags.GenerateTagSuggestionsUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import cat.company.qrreader.features.camera.presentation.BarcodeState
import cat.company.qrreader.features.camera.presentation.QrCameraViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for QrCameraViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QrCameraViewModelTest {

    // Fake GenerateTagSuggestionsUseCase for testing
    private class FakeGenerateTagSuggestionsUseCase : GenerateTagSuggestionsUseCase() {
        override suspend fun invoke(
            barcodeContent: String,
            barcodeType: String?,
            barcodeFormat: String?,
            existingTags: List<String>
        ): Result<List<SuggestedTagModel>> {
            return Result.success(emptyList())
        }

        override suspend fun downloadModelIfNeeded() {
            // No-op for tests - model is not needed in test environment
        }

        override fun cleanup() {
            // No-op for tests
        }
    }

    // Fake TagRepository for testing
    private class FakeTagRepository : TagRepository {
        override fun getAllTags(): Flow<List<TagModel>> = MutableStateFlow(emptyList())
        override fun insertTags(vararg tags: TagModel) {}
        override fun updateTag(tag: TagModel) {}
        override fun deleteTag(tag: TagModel) {}
    }

    private fun createViewModel(): QrCameraViewModel {
        return QrCameraViewModel(
            generateTagSuggestionsUseCase = FakeGenerateTagSuggestionsUseCase(),
            getAllTagsUseCase = GetAllTagsUseCase(FakeTagRepository())
        )
    }

    /**
     * Test initial state has null lastBarcode
     */
    @Test
    fun initialState_lastBarcodeIsNull() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.first()

        assertNull(state.lastBarcode)
    }

    /**
     * Test saveBarcodes with null updates state to null
     */
    @Test
    fun saveBarcodes_withNull_updatesStateToNull() = runTest {
        val viewModel = createViewModel()

        viewModel.saveBarcodes(null)

        val state = viewModel.uiState.first()
        assertNull(state.lastBarcode)
    }

    /**
     * Test saveBarcodes with empty list updates state
     */
    @Test
    fun saveBarcodes_withEmptyList_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.saveBarcodes(emptyList())

        val state = viewModel.uiState.first()
        assertNotNull(state.lastBarcode)
        assertEquals(0, state.lastBarcode?.size)
    }

    /**
     * Test uiState is exposed as StateFlow
     */
    @Test
    fun uiState_isStateFlow() {
        val viewModel = createViewModel()

        assertNotNull(viewModel.uiState)
        assertNotNull(viewModel.uiState.value)
    }

    /**
     * Test multiple saves update state correctly
     */
    @Test
    fun saveBarcodes_multipleCalls_lastCallWins() = runTest {
        val viewModel = createViewModel()

        // First save with empty list
        viewModel.saveBarcodes(emptyList())
        assertEquals(0, viewModel.uiState.first().lastBarcode?.size)

        // Second save with null
        viewModel.saveBarcodes(null)
        assertNull(viewModel.uiState.first().lastBarcode)

        // Third save with empty list again
        viewModel.saveBarcodes(emptyList())
        assertEquals(0, viewModel.uiState.first().lastBarcode?.size)
    }

    /**
     * Test BarcodeState data class default values
     */
    @Test
    fun barcodeState_defaultValues() {
        val state = BarcodeState()

        assertNull(state.lastBarcode)
    }

    /**
     * Test BarcodeState data class with value
     */
    @Test
    fun barcodeState_withEmptyList() {
        val state = BarcodeState(lastBarcode = emptyList())

        assertNotNull(state.lastBarcode)
        assertEquals(0, state.lastBarcode?.size)
    }

    /**
     * Test BarcodeState copy function
     */
    @Test
    fun barcodeState_copy_createsNewInstance() {
        val original = BarcodeState(lastBarcode = null)
        val copied = original.copy(lastBarcode = emptyList())

        assertNull(original.lastBarcode)
        assertNotNull(copied.lastBarcode)
    }

    /**
     * Test BarcodeState equality
     */
    @Test
    fun barcodeState_equality() {
        val state1 = BarcodeState(lastBarcode = null)
        val state2 = BarcodeState(lastBarcode = null)

        assertEquals(state1, state2)
    }

    /**
     * Test BarcodeState inequality
     */
    @Test
    fun barcodeState_inequality() {
        val state1 = BarcodeState(lastBarcode = null)
        val state2 = BarcodeState(lastBarcode = emptyList())

        assertNotNull(state1)
        assertNotNull(state2)
        // They should be different
        assert(state1 != state2)
    }
}
