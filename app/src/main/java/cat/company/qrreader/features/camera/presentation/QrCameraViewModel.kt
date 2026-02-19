package cat.company.qrreader.features.camera.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeDescriptionUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiDescriptionsEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiTagSuggestionsEnabledUseCase
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
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val generateBarcodeDescriptionUseCase: GenerateBarcodeDescriptionUseCase,
    private val getAiTagSuggestionsEnabledUseCase: GetAiTagSuggestionsEnabledUseCase,
    private val getAiDescriptionsEnabledUseCase: GetAiDescriptionsEnabledUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "QrCameraViewModel"
    }
    
    init {
        // Attempt to download Gemini Nano model on initialization, only if at least one AI feature is enabled
        viewModelScope.launch {
            try {
                val tagsEnabled = getAiTagSuggestionsEnabledUseCase().first()
                val descriptionsEnabled = getAiDescriptionsEnabledUseCase().first()
                if (tagsEnabled || descriptionsEnabled) {
                    Log.d(TAG, "Checking Gemini Nano model availability")
                    generateTagSuggestionsUseCase.downloadModelIfNeeded()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during model download check", e)
            }
        }
    }
    
    private val _uiState = MutableStateFlow(BarcodeState())
    val uiState: StateFlow<BarcodeState> = _uiState.asStateFlow()

    fun saveBarcodes(barcodes: List<Barcode>?) {
        _uiState.update { it.copy(lastBarcode = barcodes) }
        
        // Generate tag suggestions and descriptions for ALL barcodes
        if (!barcodes.isNullOrEmpty()) {
            barcodes.forEach { barcode ->
                val content = barcode.displayValue ?: return@forEach
                val barcodeHash = barcode.hashCode()
                
                // Get barcode type and format for better context
                val barcodeType = getBarcodeTypeName(barcode.valueType)
                val barcodeFormat = getBarcodeFormatName(barcode.format)
                
                Log.d(TAG, "Generating tags and description for barcode #$barcodeHash: content='$content', type=$barcodeType, format=$barcodeFormat")
                
                // Generate tag suggestions
                viewModelScope.launch {
                    if (!getAiTagSuggestionsEnabledUseCase().first()) return@launch

                    // Mark this barcode as loading tags
                    _uiState.update { state ->
                        state.copy(isLoadingTags = state.isLoadingTags + barcodeHash)
                    }
                    
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
                            _uiState.update { state ->
                                state.copy(
                                    barcodeTags = state.barcodeTags + (barcodeHash to suggestions),
                                    isLoadingTags = state.isLoadingTags - barcodeHash,
                                    tagSuggestionErrors = state.tagSuggestionErrors - barcodeHash
                                )
                            }
                        }.onFailure { error ->
                            Log.w(TAG, "Failed to generate tag suggestions for barcode #$barcodeHash: ${error.message}")
                            _uiState.update { state ->
                                state.copy(
                                    barcodeTags = state.barcodeTags + (barcodeHash to emptyList()),
                                    isLoadingTags = state.isLoadingTags - barcodeHash,
                                    tagSuggestionErrors = state.tagSuggestionErrors + (barcodeHash to (error.message ?: "Unknown error"))
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception generating tag suggestions for barcode #$barcodeHash", e)
                        _uiState.update { state ->
                            state.copy(
                                barcodeTags = state.barcodeTags + (barcodeHash to emptyList()),
                                isLoadingTags = state.isLoadingTags - barcodeHash,
                                tagSuggestionErrors = state.tagSuggestionErrors + (barcodeHash to (e.message ?: "Unknown error"))
                            )
                        }
                    }
                }
                
                // Generate description
                viewModelScope.launch {
                    if (!getAiDescriptionsEnabledUseCase().first()) return@launch

                    // Mark this barcode as loading description
                    _uiState.update { state ->
                        state.copy(isLoadingDescriptions = state.isLoadingDescriptions + barcodeHash)
                    }
                    
                    try {
                        val result = generateBarcodeDescriptionUseCase(
                            barcodeContent = content,
                            barcodeType = barcodeType,
                            barcodeFormat = barcodeFormat
                        )
                        
                        result.onSuccess { description ->
                            Log.d(TAG, "Generated description for barcode #$barcodeHash: $description")
                            _uiState.update { state ->
                                state.copy(
                                    barcodeDescriptions = state.barcodeDescriptions + (barcodeHash to description),
                                    isLoadingDescriptions = state.isLoadingDescriptions - barcodeHash,
                                    descriptionErrors = state.descriptionErrors - barcodeHash
                                )
                            }
                        }.onFailure { error ->
                            Log.w(TAG, "Failed to generate description for barcode #$barcodeHash: ${error.message}")
                            _uiState.update { state ->
                                state.copy(
                                    isLoadingDescriptions = state.isLoadingDescriptions - barcodeHash,
                                    descriptionErrors = state.descriptionErrors + (barcodeHash to (error.message ?: "Unknown error"))
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception generating description for barcode #$barcodeHash", e)
                        _uiState.update { state ->
                            state.copy(
                                isLoadingDescriptions = state.isLoadingDescriptions - barcodeHash,
                                descriptionErrors = state.descriptionErrors + (barcodeHash to (e.message ?: "Unknown error"))
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Toggle tag selection for a specific barcode
     */
    fun toggleTagSelection(barcodeHash: Int, tagName: String) {
        _uiState.update { state ->
            val currentTags = state.barcodeTags[barcodeHash] ?: return@update state
            val updatedTags = currentTags.map { tag ->
                if (tag.name == tagName) {
                    tag.copy(isSelected = !tag.isSelected)
                } else {
                    tag
                }
            }
            state.copy(barcodeTags = state.barcodeTags + (barcodeHash to updatedTags))
        }
    }
    
    /**
     * Get selected tag names for a specific barcode
     */
    fun getSelectedTagNames(barcodeHash: Int): List<String> {
        return _uiState.value.barcodeTags[barcodeHash]
            ?.filter { it.isSelected }
            ?.map { it.name }
            ?: emptyList()
    }
    
    /**
     * Get suggested tags for a specific barcode
     */
    fun getSuggestedTags(barcodeHash: Int): List<SuggestedTagModel> {
        return _uiState.value.barcodeTags[barcodeHash] ?: emptyList()
    }
    
    /**
     * Check if tags are loading for a specific barcode
     */
    fun isLoadingTags(barcodeHash: Int): Boolean {
        return _uiState.value.isLoadingTags.contains(barcodeHash)
    }
    
    /**
     * Get error message for a specific barcode
     */
    fun getTagError(barcodeHash: Int): String? {
        return _uiState.value.tagSuggestionErrors[barcodeHash]
    }
    
    /**
     * Get AI-generated description for a specific barcode
     */
    fun getDescription(barcodeHash: Int): String? {
        return _uiState.value.barcodeDescriptions[barcodeHash]
    }
    
    /**
     * Check if description is loading for a specific barcode
     */
    fun isLoadingDescription(barcodeHash: Int): Boolean {
        return _uiState.value.isLoadingDescriptions.contains(barcodeHash)
    }
    
    /**
     * Get description error message for a specific barcode
     */
    fun getDescriptionError(barcodeHash: Int): String? {
        return _uiState.value.descriptionErrors[barcodeHash]
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
        generateBarcodeDescriptionUseCase.cleanup()
    }
}

/**
 * State for the barcode
 * 
 * @property lastBarcode List of scanned barcodes
 * @property barcodeTags Map of barcode hash to its suggested tags
 * @property isLoadingTags Set of barcode hashes that are currently loading tags
 * @property tagSuggestionErrors Map of barcode hash to error messages
 * @property barcodeDescriptions Map of barcode hash to AI-generated description
 * @property isLoadingDescriptions Set of barcode hashes that are currently loading descriptions
 * @property descriptionErrors Map of barcode hash to description error messages
 */
data class BarcodeState(
    var lastBarcode: List<Barcode>? = null,
    val barcodeTags: Map<Int, List<SuggestedTagModel>> = emptyMap(),
    val isLoadingTags: Set<Int> = emptySet(),
    val tagSuggestionErrors: Map<Int, String> = emptyMap(),
    val barcodeDescriptions: Map<Int, String> = emptyMap(),
    val isLoadingDescriptions: Set<Int> = emptySet(),
    val descriptionErrors: Map<Int, String> = emptyMap()
)

