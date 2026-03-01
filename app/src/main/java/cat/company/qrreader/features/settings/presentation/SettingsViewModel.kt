package cat.company.qrreader.features.settings.presentation

import androidx.lifecycle.ViewModel
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetBiometricLockEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetBiometricLockEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetDuplicateCheckEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetDuplicateCheckEnabledUseCase
import cat.company.qrreader.domain.usecase.update.CheckAppUpdateUseCase
import cat.company.qrreader.domain.usecase.update.UpdateCheckResult
import cat.company.qrreader.domain.usecase.settings.GetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.GetShowTagCountersUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiGenerationEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiHumorousDescriptionsUseCase
import cat.company.qrreader.domain.usecase.settings.SetAiLanguageUseCase
import cat.company.qrreader.domain.usecase.settings.SetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.SetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetShowTagCountersUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * History display/filter settings use cases grouped for constructor injection.
 *
 * Encapsulates the use cases that read and write history display and filter preferences.
 *
 * @property getHideTaggedSetting Reads the "hide tagged when no tag selected" preference.
 * @property setHideTaggedSetting Writes the "hide tagged when no tag selected" preference.
 * @property getSearchAcrossAllTags Reads the "search across all tags when filtering" preference.
 * @property setSearchAcrossAllTags Writes the "search across all tags when filtering" preference.
 * @property getShowTagCounters Reads the "show tag counters" preference.
 * @property setShowTagCounters Writes the "show tag counters" preference.
 */
data class HistoryFilterSettingsUseCases(
    val getHideTaggedSetting: GetHideTaggedSettingUseCase,
    val setHideTaggedSetting: SetHideTaggedSettingUseCase,
    val getSearchAcrossAllTags: GetSearchAcrossAllTagsUseCase,
    val setSearchAcrossAllTags: SetSearchAcrossAllTagsUseCase,
    val getShowTagCounters: GetShowTagCountersUseCase,
    val setShowTagCounters: SetShowTagCountersUseCase
)

/**
 * History privacy/security settings use cases grouped for constructor injection.
 *
 * Encapsulates the use cases that read and write privacy and security-related preferences.
 *
 * @property getBiometricLockEnabled Reads the biometric lock enabled preference.
 * @property setBiometricLockEnabled Writes the biometric lock enabled preference.
 * @property getDuplicateCheckEnabled Reads the duplicate scan check enabled preference.
 * @property setDuplicateCheckEnabled Writes the duplicate scan check enabled preference.
 */
data class HistoryPrivacySettingsUseCases(
    val getBiometricLockEnabled: GetBiometricLockEnabledUseCase,
    val setBiometricLockEnabled: SetBiometricLockEnabledUseCase,
    val getDuplicateCheckEnabled: GetDuplicateCheckEnabledUseCase,
    val setDuplicateCheckEnabled: SetDuplicateCheckEnabledUseCase
)

/**
 * AI-related settings use cases grouped for constructor injection.
 *
 * Encapsulates all use cases for reading/writing AI feature preferences and checking device
 * support.
 *
 * @property getAiGenerationEnabled Reads the AI generation enabled flag.
 * @property setAiGenerationEnabled Writes the AI generation enabled flag.
 * @property getAiLanguage Reads the AI response language preference.
 * @property setAiLanguage Writes the AI response language preference.
 * @property getAiHumorousDescriptions Reads the humorous descriptions toggle.
 * @property setAiHumorousDescriptions Writes the humorous descriptions toggle.
 * @property generateBarcodeAiData Provides device-support check for Gemini Nano.
 */
data class AiSettingsUseCases(
    val getAiGenerationEnabled: GetAiGenerationEnabledUseCase,
    val setAiGenerationEnabled: SetAiGenerationEnabledUseCase,
    val getAiLanguage: GetAiLanguageUseCase,
    val setAiLanguage: SetAiLanguageUseCase,
    val getAiHumorousDescriptions: GetAiHumorousDescriptionsUseCase,
    val setAiHumorousDescriptions: SetAiHumorousDescriptionsUseCase,
    val generateBarcodeAiData: GenerateBarcodeAiDataUseCase
)

/**
 * ViewModel for Settings screen
 * Manages settings state and coordinates use cases
 */
class SettingsViewModel(
    private val filterSettings: HistoryFilterSettingsUseCases,
    private val privacySettings: HistoryPrivacySettingsUseCases,
    private val aiSettings: AiSettingsUseCases,
    private val checkAppUpdateUseCase: CheckAppUpdateUseCase
) : ViewModel() {

    private val _isAiAvailableOnDevice = MutableStateFlow(false)
    private val _updateCheckResult = MutableStateFlow<UpdateCheckResult?>(null)
    private val _isCheckingForUpdates = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _isAiAvailableOnDevice.value = aiSettings.generateBarcodeAiData.isAiSupportedOnDevice()
        }
    }

    /**
     * Whether AI features are available on the current device (Gemini Nano support).
     * When `false`, the AI settings section should be hidden entirely.
     */
    val isAiAvailableOnDevice: StateFlow<Boolean> = _isAiAvailableOnDevice.asStateFlow()

    /** The most recent update check result, or `null` if no check has been performed yet. */
    val updateCheckResult: StateFlow<UpdateCheckResult?> = _updateCheckResult.asStateFlow()

    /** `true` while an update check network request is in progress. */
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()

    /**
     * Flow of the hide tagged when no tag selected setting
     */
    val hideTaggedWhenNoTagSelected: Flow<Boolean> = filterSettings.getHideTaggedSetting()

    /**
     * Flow for the 'search across all tags when filtering' setting
     */
    val searchAcrossAllTagsWhenFiltering: Flow<Boolean> = filterSettings.getSearchAcrossAllTags()

    /**
     * Flow for the 'show tag counters' setting
     */
    val showTagCounters: Flow<Boolean> = filterSettings.getShowTagCounters()

    /**
     * Flow for the biometric lock enabled setting
     */
    val biometricLockEnabled: Flow<Boolean> = privacySettings.getBiometricLockEnabled()

    /**
     * Flow for the duplicate scan check enabled setting
     */
    val duplicateCheckEnabled: Flow<Boolean> = privacySettings.getDuplicateCheckEnabled()

    /**
     * Flow for the 'AI generation enabled' setting
     */
    val aiGenerationEnabled: Flow<Boolean> = aiSettings.getAiGenerationEnabled()

    /**
     * Flow for the AI language setting
     */
    val aiLanguage: Flow<String> = aiSettings.getAiLanguage()

    /**
     * Flow for the 'AI humorous descriptions' setting
     */
    val aiHumorousDescriptions: Flow<Boolean> = aiSettings.getAiHumorousDescriptions()

    /**
     * Update the hide tagged when no tag selected setting
     */
    fun setHideTaggedWhenNoTagSelected(value: Boolean) {
        viewModelScope.launch {
            filterSettings.setHideTaggedSetting(value)
        }
    }

    fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {
        viewModelScope.launch {
            filterSettings.setSearchAcrossAllTags(value)
        }
    }

    fun setShowTagCounters(value: Boolean) {
        viewModelScope.launch {
            filterSettings.setShowTagCounters(value)
        }
    }

    fun setBiometricLockEnabled(value: Boolean) {
        viewModelScope.launch {
            privacySettings.setBiometricLockEnabled(value)
        }
    }

    fun setDuplicateCheckEnabled(value: Boolean) {
        viewModelScope.launch {
            privacySettings.setDuplicateCheckEnabled(value)
        }
    }

    fun setAiGenerationEnabled(value: Boolean) {
        viewModelScope.launch {
            aiSettings.setAiGenerationEnabled(value)
        }
    }

    fun setAiLanguage(value: String) {
        viewModelScope.launch {
            aiSettings.setAiLanguage(value)
        }
    }

    fun setAiHumorousDescriptions(value: Boolean) {
        viewModelScope.launch {
            aiSettings.setAiHumorousDescriptions(value)
        }
    }

    /** Triggers a remote update check and updates [updateCheckResult]. */
    fun checkForUpdates() {
        if (_isCheckingForUpdates.value) return
        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            try {
                _updateCheckResult.value = checkAppUpdateUseCase()
            } finally {
                _isCheckingForUpdates.value = false
            }
        }
    }

    /** Clears the last update check result (e.g. after the user dismisses the result dialog). */
    fun clearUpdateCheckResult() {
        _updateCheckResult.value = null
    }

    /** Records an error that occurred while trying to launch the in-app update flow. */
    fun onUpdateFlowFailed(message: String) {
        _updateCheckResult.value = UpdateCheckResult.Error(message)
    }
}
