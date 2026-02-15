package cat.company.qrreader.features.camera.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.tags.GenerateTagSuggestionsUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the camera
 */
class QrCameraViewModel(
    private val generateTagSuggestionsUseCase: GenerateTagSuggestionsUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "QrCameraViewModel"
    }
    
    private val _uiState = MutableStateFlow(BarcodeState())
    val uiState: StateFlow<BarcodeState> = _uiState.asStateFlow()

    fun saveBarcodes(barcodes: List<Barcode>?) {
        _uiState.update { it.copy(lastBarcode = barcodes) }
        
        // Generate tag suggestions for the first barcode
        if (!barcodes.isNullOrEmpty()) {
            val firstBarcode = barcodes.first()
            val content = firstBarcode.displayValue ?: return
            
            // Get barcode type and format for better context
            val barcodeType = getBarcodeTypeName(firstBarcode.valueType)
            val barcodeFormat = getBarcodeFormatName(firstBarcode.format)
            
            Log.d(TAG, "Generating tags for barcode: content='$content', type=$barcodeType, format=$barcodeFormat")
            
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingTagSuggestions = true) }
                
                try {
                    // Get existing tag names
                    val existingTags = getAllTagsUseCase().first()
                    val existingTagNames = existingTags.map { it.name }
                    
                    // Generate suggestions with full barcode context
                    val result = generateTagSuggestionsUseCase(
                        barcodeContent = content,
                        barcodeType = barcodeType,
                        barcodeFormat = barcodeFormat,
                        existingTags = existingTagNames
                    )
                    
                    result.onSuccess { suggestions ->
                        _uiState.update { 
                            it.copy(
                                suggestedTags = suggestions,
                                isLoadingTagSuggestions = false,
                                tagSuggestionError = null
                            ) 
                        }
                    }.onFailure { error ->
                        Log.w("QrCameraViewModel", "Failed to generate tag suggestions: ${error.message}")
                        _uiState.update { 
                            it.copy(
                                suggestedTags = emptyList(),
                                isLoadingTagSuggestions = false,
                                tagSuggestionError = error.message
                            ) 
                        }
                    }
                } catch (e: Exception) {
                    Log.e("QrCameraViewModel", "Error generating tag suggestions", e)
                    _uiState.update { 
                        it.copy(
                            suggestedTags = emptyList(),
                            isLoadingTagSuggestions = false,
                            tagSuggestionError = e.message
                        ) 
                    }
                }
            }
        }
    }
    
    fun toggleTagSelection(tagName: String) {
        _uiState.update { state ->
            val updatedTags = state.suggestedTags.map { tag ->
                if (tag.name == tagName) {
                    tag.copy(isSelected = !tag.isSelected)
                } else {
                    tag
                }
            }
            state.copy(suggestedTags = updatedTags)
        }
    }
    
    fun getSelectedTagNames(): List<String> {
        return _uiState.value.suggestedTags
            .filter { it.isSelected }
            .map { it.name }
    }
    
    /**
     * Convert ML Kit barcode value type to human-readable name
     */
    private fun getBarcodeTypeName(valueType: Int): String {
        return when (valueType) {
            Barcode.TYPE_CONTACT_INFO -> "Contact"
            Barcode.TYPE_EMAIL -> "Email"
            Barcode.TYPE_ISBN -> "ISBN"
            Barcode.TYPE_PHONE -> "Phone"
            Barcode.TYPE_PRODUCT -> "Product"
            Barcode.TYPE_SMS -> "SMS"
            Barcode.TYPE_TEXT -> "Text"
            Barcode.TYPE_URL -> "URL"
            Barcode.TYPE_WIFI -> "Wi-Fi"
            Barcode.TYPE_GEO -> "Location"
            Barcode.TYPE_CALENDAR_EVENT -> "Calendar"
            Barcode.TYPE_DRIVER_LICENSE -> "Driver License"
            else -> "Unknown"
        }
    }
    
    /**
     * Convert ML Kit barcode format to human-readable name
     */
    private fun getBarcodeFormatName(format: Int): String {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> "QR Code"
            Barcode.FORMAT_AZTEC -> "Aztec"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODABAR -> "Codabar"
            Barcode.FORMAT_ITF -> "ITF"
            else -> "Unknown"
        }
    }

    override fun onCleared() {
        super.onCleared()
        generateTagSuggestionsUseCase.cleanup()
    }
}

/**
 * State for the barcode
 */
data class BarcodeState(
    var lastBarcode: List<Barcode>? = null,
    val suggestedTags: List<SuggestedTagModel> = emptyList(),
    val isLoadingTagSuggestions: Boolean = false,
    val tagSuggestionError: String? = null
)

