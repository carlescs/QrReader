package cat.company.qrreader.camera

import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QrCameraViewModel: ViewModel() {
    private val _uiState= MutableStateFlow(BarcodeState())
    val uiState: StateFlow<BarcodeState> = _uiState.asStateFlow()

    fun saveBarcodes(barcodes: List<Barcode>?) {
        _uiState.value = BarcodeState(barcodes)
    }
}

data class BarcodeState(
    val lastBarcode: List<Barcode>?=null
)