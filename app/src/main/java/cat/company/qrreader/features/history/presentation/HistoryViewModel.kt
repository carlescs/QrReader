package cat.company.qrreader.features.history.presentation

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.repository.SettingsRepository
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.ToggleFavoriteUseCase
import cat.company.qrreader.domain.usecase.history.ToggleLockBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Barcode manipulation use cases grouped for constructor injection.
 *
 * Encapsulates all use cases that read and modify barcode data in the history feature.
 *
 * @property getBarcodes Fetches barcodes with their associated tags, applying filters.
 * @property update Persists changes to an existing barcode.
 * @property delete Permanently removes a barcode from the database.
 * @property toggleFavorite Toggles the favorite (starred) state for a barcode.
 * @property toggleLock Toggles the locked (hidden) state for a barcode.
 */
data class HistoryBarcodeUseCases(
    val getBarcodes: GetBarcodesWithTagsUseCase,
    val update: UpdateBarcodeUseCase,
    val delete: DeleteBarcodeUseCase,
    val toggleFavorite: ToggleFavoriteUseCase,
    val toggleLock: ToggleLockBarcodeUseCase
)

/**
 * AI generation use cases grouped for constructor injection.
 *
 * Encapsulates the use cases and settings required to generate AI-powered descriptions
 * and tag suggestions for barcodes in the history feature.
 *
 * @property generateBarcodeAiData Generates AI descriptions and tag suggestions on-device.
 * @property getLanguage Reads the AI response language preference.
 * @property getHumorousDescriptions Reads the humorous descriptions toggle.
 */
data class HistoryAiUseCases(
    val generateBarcodeAiData: GenerateBarcodeAiDataUseCase,
    val getLanguage: GetAiLanguageUseCase,
    val getHumorousDescriptions: GetAiHumorousDescriptionsUseCase
)

/**
 * ViewModel for the history
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val barcodeUseCases: HistoryBarcodeUseCases,
    settingsRepository: SettingsRepository,
    private val aiUseCases: HistoryAiUseCases,
    private val processLifecycleOwner: LifecycleOwner? = null
) : ViewModel() {

    private val _selectedTagId = MutableStateFlow<Int?>(null)
    val selectedTagId = _selectedTagId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites = _showOnlyFavorites.asStateFlow()

    private val _isAiSupportedOnDevice = MutableStateFlow(false)

    private val appLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            if (biometricLockEnabled.value) {
                lockAllBarcodes()
            }
        }
    }

    init {
        processLifecycleOwner?.lifecycle?.addObserver(appLifecycleObserver)
        viewModelScope.launch {
            _isAiSupportedOnDevice.value = aiUseCases.generateBarcodeAiData.isAiSupportedOnDevice()
        }
    }

    /** Whether AI generation features are available (device support AND user setting enabled). */
    val aiGenerationEnabled: StateFlow<Boolean> =
        combine(settingsRepository.aiGenerationEnabled, _isAiSupportedOnDevice) { enabled, supported ->
            enabled && supported
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Whether biometric lock feature is enabled. */
    val biometricLockEnabled: StateFlow<Boolean> =
        settingsRepository.biometricLockEnabled
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _unlockedBarcodeIds = MutableStateFlow<Set<Int>>(emptySet())
    val unlockedBarcodeIds: StateFlow<Set<Int>> = _unlockedBarcodeIds.asStateFlow()

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
                if (showFavorites) {
                    // Favorites filter takes precedence: ignore tag, search, and hide-tagged filters
                    barcodeUseCases.getBarcodes(null, null, false, false, true)
                } else {
                    val q = query.takeIf { it.isNotBlank() }
                    val effectiveTagId = if (searchAcrossAll && q != null) null else tagId
                    barcodeUseCases.getBarcodes(effectiveTagId, q, hideTagged, searchAcrossAll, false)
                }
            }

    fun onTagSelected(tagId: Int?) {
        _selectedTagId.value = tagId
        _showOnlyFavorites.value = false
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        val turningOn = !_showOnlyFavorites.value
        _showOnlyFavorites.value = turningOn
        if (turningOn) {
            _selectedTagId.value = null
        }
    }

    fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            barcodeUseCases.toggleFavorite(barcodeId, isFavorite)
        }
    }

    fun toggleLockBarcode(barcodeId: Int, isLocked: Boolean) {
        viewModelScope.launch {
            barcodeUseCases.toggleLock(barcodeId, isLocked)
            // Always remove from in-memory unlock set when changing the persistent lock state.
            // If locking (isLocked=true): the barcode may currently be in the unlocked set
            //   (user was viewing it before deciding to lock it), so we must evict it.
            // If unlocking persistently (isLocked=false): cleanup is safe; the DB state (false)
            //   already makes the barcode visible, so the set entry is redundant.
            _unlockedBarcodeIds.update { it - barcodeId }
        }
    }

    fun markBarcodeUnlocked(barcodeId: Int) {
        _unlockedBarcodeIds.update { it + barcodeId }
    }

    /**
     * Clears all temporarily unlocked barcodes from the in-memory cache.
     *
     * This does NOT modify the persistent lock state in the database. Barcodes that have
     * [BarcodeModel.isLocked] = true will appear locked again after this call, requiring
     * fresh biometric authentication to view them.
     */
    fun lockAllBarcodes() {
        _unlockedBarcodeIds.value = emptySet()
    }

    fun updateBarcode(barcode: BarcodeModel) {
        viewModelScope.launch {
            val rows = barcodeUseCases.update(barcode)
            Log.d("HistoryViewModel", "updateBarcode id=${barcode.id} rows=$rows")
        }
    }

    fun deleteBarcode(barcode: BarcodeModel) {
        viewModelScope.launch {
            barcodeUseCases.delete(barcode)
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
            val language = aiUseCases.getLanguage().first()
            val humorous = aiUseCases.getHumorousDescriptions().first()
            val result = aiUseCases.generateBarcodeAiData(
                barcodeContent = barcode.barcode,
                barcodeType = getBarcodeTypeName(barcode.type),
                barcodeFormat = getBarcodeFormatName(barcode.format),
                existingTags = emptyList(),
                language = language,
                humorous = humorous,
                userTitle = barcode.title,
                userDescription = barcode.description
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

    /**
     * State for AI tag suggestions on a specific history barcode.
     *
     * @property isLoading Whether the AI generation is in progress
     * @property suggestedTags List of AI-suggested tags (not yet added to the barcode)
     * @property error The error message, if generation failed
     */
    data class TagSuggestionState(
        val isLoading: Boolean = false,
        val suggestedTags: List<SuggestedTagModel> = emptyList(),
        val error: String? = null
    )

    private val _tagSuggestionStates = MutableStateFlow<Map<Int, TagSuggestionState>>(emptyMap())
    val tagSuggestionStates = _tagSuggestionStates.asStateFlow()

    /**
     * Generate AI tag suggestions for a history barcode and update [tagSuggestionStates].
     *
     * @param barcode The barcode to suggest tags for
     * @param existingTagNames Names of all tags already in the user's library
     */
    fun suggestTags(barcode: BarcodeWithTagsModel, existingTagNames: List<String>) {
        val barcodeId = barcode.barcode.id
        _tagSuggestionStates.update { it + (barcodeId to TagSuggestionState(isLoading = true)) }
        viewModelScope.launch {
            val language = aiUseCases.getLanguage().first()
            val humorous = aiUseCases.getHumorousDescriptions().first()
            val result = aiUseCases.generateBarcodeAiData(
                barcodeContent = barcode.barcode.barcode,
                barcodeType = getBarcodeTypeName(barcode.barcode.type),
                barcodeFormat = getBarcodeFormatName(barcode.barcode.format),
                existingTags = existingTagNames,
                language = language,
                humorous = humorous,
                userTitle = barcode.barcode.title,
                userDescription = barcode.barcode.description
            )
            result.fold(
                onSuccess = { aiData ->
                    _tagSuggestionStates.update {
                        it + (barcodeId to TagSuggestionState(
                            suggestedTags = aiData.tags.map { tag -> tag.copy(isSelected = false) }
                        ))
                    }
                },
                onFailure = { e ->
                    _tagSuggestionStates.update {
                        it + (barcodeId to TagSuggestionState(error = e.message ?: "Unknown error"))
                    }
                }
            )
        }
    }

    /**
     * Clear AI tag suggestion state for a specific barcode.
     *
     * Called when tag editing is closed so that the next time the user opens tag editing
     * for this barcode, fresh suggestions can be generated if needed.
     */
    fun resetTagSuggestionState(barcodeId: Int) {
        _tagSuggestionStates.update { current ->
            if (current.isEmpty()) current else current - barcodeId
        }
    }

    override fun onCleared() {
        super.onCleared()
        processLifecycleOwner?.lifecycle?.removeObserver(appLifecycleObserver)
        aiUseCases.generateBarcodeAiData.cleanup()
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
