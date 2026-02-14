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
    private val _uiState = MutableStateFlow(BarcodeState())
    val uiState: StateFlow<BarcodeState> = _uiState.asStateFlow()

    fun saveBarcodes(barcodes: List<Barcode>?) {
        _uiState.update { it.copy(lastBarcode = barcodes) }
        
        // Generate tag suggestions for the first barcode
        if (!barcodes.isNullOrEmpty()) {
            val firstBarcode = barcodes.first()
            val content = firstBarcode.displayValue ?: return
            
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingTagSuggestions = true) }
                
                try {
                    // Get existing tag names
                    val existingTags = getAllTagsUseCase().first()
                    val existingTagNames = existingTags.map { it.name }
                    
                    // Generate suggestions
                    val result = generateTagSuggestionsUseCase(content, existingTagNames)
                    
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

