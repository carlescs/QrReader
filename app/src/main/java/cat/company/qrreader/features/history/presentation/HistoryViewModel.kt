package cat.company.qrreader.features.history.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.ToggleFavoriteUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.utils.getBarcodeFormatName
import cat.company.qrreader.utils.getBarcodeTypeName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the history
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getBarcodesWithTagsUseCase: GetBarcodesWithTagsUseCase,
    private val updateBarcodeUseCase: UpdateBarcodeUseCase,
    private val deleteBarcodeUseCase: DeleteBarcodeUseCase,
    settingsRepository: SettingsRepository,
    private val generateBarcodeAiDataUseCase: GenerateBarcodeAiDataUseCase,
    private val getAiLanguageUseCase: GetAiLanguageUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _selectedTagId = MutableStateFlow<Int?>(null)
    val selectedTagId = _selectedTagId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites = _showOnlyFavorites.asStateFlow()

    private val _isAiSupportedOnDevice = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _isAiSupportedOnDevice.value = generateBarcodeAiDataUseCase.isAiSupportedOnDevice()
        }
    }

    /** Whether AI generation features are available (device support AND user setting enabled). */
    val aiGenerationEnabled: StateFlow<Boolean> =
        combine(settingsRepository.aiGenerationEnabled, _isAiSupportedOnDevice) { enabled, supported ->
            enabled && supported
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Debounce input to avoid querying DB on every keystroke
    @OptIn(FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(250)
        .map { it.trim() }
        .distinctUntilChanged()

    val savedBarcodes: Flow<List<BarcodeWithTagsModel>> =
        combine(
            _selectedTagId,
            debouncedQuery,
            settingsRepository.hideTaggedWhenNoTagSelected,
            settingsRepository.searchAcrossAllTagsWhenFiltering,
            _showOnlyFavorites
        ) { tagId, query, hideTagged, searchAcrossAll, showFavorites ->
            FilterParams(tagId, query, hideTagged, searchAcrossAll, showFavorites)
        }
            .flatMapLatest { (tagId, query, hideTagged, searchAcrossAll, showFavorites) ->
                val q = query.takeIf { it.isNotBlank() }
                val effectiveTagId = if (searchAcrossAll && q != null) null else tagId
                getBarcodesWithTagsUseCase(effectiveTagId, q, hideTagged, searchAcrossAll, showFavorites)
            }

    fun onTagSelected(tagId: Int?) {
        _selectedTagId.value = tagId
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(barcodeId, isFavorite)
        }
    }

    fun updateBarcode(barcode: BarcodeModel) {
        viewModelScope.launch {
            val rows = updateBarcodeUseCase(barcode)
            Log.d("HistoryViewModel", "updateBarcode id=${barcode.id} rows=$rows")
        }
    }

    fun deleteBarcode(barcode: BarcodeModel) {
        viewModelScope.launch {
            deleteBarcodeUseCase(barcode)
        }
    }

    /**
     * State for AI description regeneration in the edit dialog.
     *
     * @property isLoading Whether the AI generation is in progress
     * @property description The newly generated description, if successful
     * @property error The error message, if generation failed
     */
    data class RegenerateDescriptionState(
        val isLoading: Boolean = false,
        val description: String? = null,
        val error: String? = null
    )

    private val _regenerateDescriptionState = MutableStateFlow(RegenerateDescriptionState())
    val regenerateDescriptionState = _regenerateDescriptionState.asStateFlow()

    /**
     * Regenerate the AI description for a barcode and update [regenerateDescriptionState].
     */
    fun regenerateAiDescription(barcode: BarcodeModel) {
        viewModelScope.launch {
            _regenerateDescriptionState.value = RegenerateDescriptionState(isLoading = true)
            val language = getAiLanguageUseCase().first()
            val result = generateBarcodeAiDataUseCase(
                barcodeContent = barcode.barcode,
                barcodeType = getBarcodeTypeName(barcode.type),
                barcodeFormat = getBarcodeFormatName(barcode.format),
                existingTags = emptyList(),
                language = language
            )
            result.fold(
                onSuccess = { aiData ->
                    _regenerateDescriptionState.value = RegenerateDescriptionState(description = aiData.description)
                },
                onFailure = { e ->
                    _regenerateDescriptionState.value = RegenerateDescriptionState(error = e.message ?: "Unknown error")
                }
            )
        }
    }

    /**
     * Reset the regeneration state (e.g., when the edit dialog is closed).
     */
    fun resetRegenerateDescriptionState() {
        _regenerateDescriptionState.value = RegenerateDescriptionState()
    }

    override fun onCleared() {
        super.onCleared()
        generateBarcodeAiDataUseCase.cleanup()
    }

    // small data holder for combine result
    private data class FilterParams(
        val tagId: Int?,
        val query: String,
        val hideTagged: Boolean,
        val searchAcrossAll: Boolean,
        val showFavorites: Boolean
    )
}
