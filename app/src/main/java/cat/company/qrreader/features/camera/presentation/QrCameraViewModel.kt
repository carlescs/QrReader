package cat.company.qrreader.features.camera.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import cat.company.qrreader.utils.getBarcodeFormatName
import cat.company.qrreader.utils.getBarcodeTypeName
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the camera
 */
class QrCameraViewModel(
    private val generateBarcodeAiDataUseCase: GenerateBarcodeAiDataUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getAiGenerationEnabledUseCase: GetAiGenerationEnabledUseCase,
    private val getAiLanguageUseCase: GetAiLanguageUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "QrCameraViewModel"
    }

    private val _isAiSupportedOnDevice = MutableStateFlow(false)

    init {
        // Check device AI support and combine with user setting to compute visible AI state
        viewModelScope.launch {
            try {
                val supported = generateBarcodeAiDataUseCase.isAiSupportedOnDevice()
                _isAiSupportedOnDevice.value = supported
                if (supported && getAiGenerationEnabledUseCase().first()) {
                    Log.d(TAG, "Checking Gemini Nano model availability")
                    generateBarcodeAiDataUseCase.downloadModelIfNeeded()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during model download check", e)
            }
        }
        // Observe combined AI generation setting (user preference AND device support)
        viewModelScope.launch {
            combine(getAiGenerationEnabledUseCase(), _isAiSupportedOnDevice) { enabled, supported ->
                enabled && supported
            }.collectLatest { aiVisible ->
                _uiState.update { it.copy(aiGenerationEnabled = aiVisible) }
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
                
                // Generate tags and description in a single AI request
                viewModelScope.launch {
                    if (!_uiState.value.aiGenerationEnabled) return@launch

                    // Mark this barcode as loading both tags and description
                    _uiState.update { state ->
                        state.copy(
                            isLoadingTags = state.isLoadingTags + barcodeHash,
                            isLoadingDescriptions = state.isLoadingDescriptions + barcodeHash
                        )
                    }

                    try {
                        val existingTags = getAllTagsUseCase().first()
                        val existingTagNames = existingTags.map { it.name }
                        val language = getAiLanguageUseCase().first()

                        val result = generateBarcodeAiDataUseCase(
                            barcodeContent = content,
                            barcodeType = barcodeType,
                            barcodeFormat = barcodeFormat,
                            existingTags = existingTagNames,
                            language = language
                        )

                        result.onSuccess { aiData ->
                            Log.d(TAG, "Generated AI data for barcode #$barcodeHash: ${aiData.tags.size} tags, description=${aiData.description}")
                            _uiState.update { state ->
                                state.copy(
                                    barcodeTags = state.barcodeTags + (barcodeHash to aiData.tags),
                                    isLoadingTags = state.isLoadingTags - barcodeHash,
                                    tagSuggestionErrors = state.tagSuggestionErrors - barcodeHash,
                                    barcodeDescriptions = state.barcodeDescriptions + (barcodeHash to aiData.description),
                                    isLoadingDescriptions = state.isLoadingDescriptions - barcodeHash,
                                    descriptionErrors = state.descriptionErrors - barcodeHash
                                )
                            }
                        }.onFailure { error ->
                            Log.w(TAG, "Failed to generate AI data for barcode #$barcodeHash: ${error.message}")
                            _uiState.update { state ->
                                state.copy(
                                    barcodeTags = state.barcodeTags + (barcodeHash to emptyList()),
                                    isLoadingTags = state.isLoadingTags - barcodeHash,
                                    tagSuggestionErrors = state.tagSuggestionErrors + (barcodeHash to (error.message ?: "Unknown error")),
                                    isLoadingDescriptions = state.isLoadingDescriptions - barcodeHash,
                                    descriptionErrors = state.descriptionErrors + (barcodeHash to (error.message ?: "Unknown error"))
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception generating AI data for barcode #$barcodeHash", e)
                        _uiState.update { state ->
                            state.copy(
                                barcodeTags = state.barcodeTags + (barcodeHash to emptyList()),
                                isLoadingTags = state.isLoadingTags - barcodeHash,
                                tagSuggestionErrors = state.tagSuggestionErrors + (barcodeHash to (e.message ?: "Unknown error")),
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
    
    override fun onCleared() {
        super.onCleared()
        generateBarcodeAiDataUseCase.cleanup()
    }
}

/**
 * State for the barcode
 * 
 * @property lastBarcode List of scanned barcodes
 * @property aiGenerationEnabled Whether AI features are enabled in settings
 * @property barcodeTags Map of barcode hash to its suggested tags
 * @property isLoadingTags Set of barcode hashes that are currently loading tags
 * @property tagSuggestionErrors Map of barcode hash to error messages
 * @property barcodeDescriptions Map of barcode hash to AI-generated description
 * @property isLoadingDescriptions Set of barcode hashes that are currently loading descriptions
 * @property descriptionErrors Map of barcode hash to description error messages
 */
data class BarcodeState(
    var lastBarcode: List<Barcode>? = null,
    val aiGenerationEnabled: Boolean = true,
    val barcodeTags: Map<Int, List<SuggestedTagModel>> = emptyMap(),
    val isLoadingTags: Set<Int> = emptySet(),
    val tagSuggestionErrors: Map<Int, String> = emptyMap(),
    val barcodeDescriptions: Map<Int, String> = emptyMap(),
    val isLoadingDescriptions: Set<Int> = emptySet(),
    val descriptionErrors: Map<Int, String> = emptyMap()
)

