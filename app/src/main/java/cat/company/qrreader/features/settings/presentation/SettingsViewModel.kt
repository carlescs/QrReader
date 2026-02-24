package cat.company.qrreader.features.settings.presentation

import androidx.lifecycle.ViewModel
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.SetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.SetSearchAcrossAllTagsUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen
 * Manages settings state and coordinates use cases
 */
class SettingsViewModel(
    getHideTaggedSettingUseCase: GetHideTaggedSettingUseCase,
    private val setHideTaggedSettingUseCase: SetHideTaggedSettingUseCase,
    getSearchAcrossAllTagsUseCase: GetSearchAcrossAllTagsUseCase,
    private val setSearchAcrossAllTagsUseCase: SetSearchAcrossAllTagsUseCase,
    getAiGenerationEnabledUseCase: GetAiGenerationEnabledUseCase,
    private val setAiGenerationEnabledUseCase: SetAiGenerationEnabledUseCase,
    getAiLanguageUseCase: GetAiLanguageUseCase,
    private val setAiLanguageUseCase: SetAiLanguageUseCase,
    getAiHumorousDescriptionsUseCase: GetAiHumorousDescriptionsUseCase,
    private val setAiHumorousDescriptionsUseCase: SetAiHumorousDescriptionsUseCase,
    private val generateBarcodeAiDataUseCase: GenerateBarcodeAiDataUseCase
) : ViewModel() {

    private val _isAiAvailableOnDevice = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _isAiAvailableOnDevice.value = generateBarcodeAiDataUseCase.isAiSupportedOnDevice()
        }
    }

    /**
     * Whether AI features are available on the current device (Gemini Nano support).
     * When `false`, the AI settings section should be hidden entirely.
     */
    val isAiAvailableOnDevice: StateFlow<Boolean> = _isAiAvailableOnDevice.asStateFlow()

    /**
     * Flow of the hide tagged when no tag selected setting
     */
    val hideTaggedWhenNoTagSelected: Flow<Boolean> = getHideTaggedSettingUseCase()

    /**
     * Flow for the 'search across all tags when filtering' setting
     */
    val searchAcrossAllTagsWhenFiltering: Flow<Boolean> = getSearchAcrossAllTagsUseCase()

    /**
     * Flow for the 'AI generation enabled' setting
     */
    val aiGenerationEnabled: Flow<Boolean> = getAiGenerationEnabledUseCase()

    /**
     * Flow for the AI language setting
     */
    val aiLanguage: Flow<String> = getAiLanguageUseCase()

    /**
     * Flow for the 'AI humorous descriptions' setting
     */
    val aiHumorousDescriptions: Flow<Boolean> = getAiHumorousDescriptionsUseCase()

    /**
     * Update the hide tagged when no tag selected setting
     */
    fun setHideTaggedWhenNoTagSelected(value: Boolean) {
        viewModelScope.launch {
            setHideTaggedSettingUseCase(value)
        }
    }

    fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {
        viewModelScope.launch {
            setSearchAcrossAllTagsUseCase(value)
        }
    }

    fun setAiGenerationEnabled(value: Boolean) {
        viewModelScope.launch {
            setAiGenerationEnabledUseCase(value)
        }
    }

    fun setAiLanguage(value: String) {
        viewModelScope.launch {
            setAiLanguageUseCase(value)
        }
    }

    fun setAiHumorousDescriptions(value: Boolean) {
        viewModelScope.launch {
            setAiHumorousDescriptionsUseCase(value)
        }
    }
}
