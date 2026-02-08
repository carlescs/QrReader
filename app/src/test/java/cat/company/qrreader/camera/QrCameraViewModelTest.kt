package cat.company.qrreader.camera

import cat.company.qrreader.features.camera.presentation.BarcodeState
import cat.company.qrreader.features.camera.presentation.QrCameraViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    /**
     * Test initial state has null lastBarcode
     */
    @Test
    fun initialState_lastBarcodeIsNull() = runTest {
        val viewModel = QrCameraViewModel()

        val state = viewModel.uiState.first()

        assertNull(state.lastBarcode)
    }

    /**
     * Test saveBarcodes with null updates state to null
     */
    @Test
    fun saveBarcodes_withNull_updatesStateToNull() = runTest {
        val viewModel = QrCameraViewModel()

        viewModel.saveBarcodes(null)

        val state = viewModel.uiState.first()
        assertNull(state.lastBarcode)
    }

    /**
     * Test saveBarcodes with empty list updates state
     */
    @Test
    fun saveBarcodes_withEmptyList_updatesState() = runTest {
        val viewModel = QrCameraViewModel()

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
        val viewModel = QrCameraViewModel()

        assertNotNull(viewModel.uiState)
        assertNotNull(viewModel.uiState.value)
    }

    /**
     * Test multiple saves update state correctly
     */
    @Test
    fun saveBarcodes_multipleCalls_lastCallWins() = runTest {
        val viewModel = QrCameraViewModel()

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
