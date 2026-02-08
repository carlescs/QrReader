package cat.company.qrreader.features.settings.presentation

import androidx.lifecycle.ViewModel
import cat.company.qrreader.domain.usecase.settings.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.SetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.settings.GetSearchAcrossAllTagsUseCase
import cat.company.qrreader.domain.usecase.settings.SetSearchAcrossAllTagsUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen
 * Manages settings state and coordinates use cases
 */
class SettingsViewModel(
    getHideTaggedSettingUseCase: GetHideTaggedSettingUseCase,
    private val setHideTaggedSettingUseCase: SetHideTaggedSettingUseCase
    , getSearchAcrossAllTagsUseCase: GetSearchAcrossAllTagsUseCase,
    private val setSearchAcrossAllTagsUseCase: SetSearchAcrossAllTagsUseCase
) : ViewModel() {

    /**
     * Flow of the hide tagged when no tag selected setting
     */
    val hideTaggedWhenNoTagSelected: Flow<Boolean> = getHideTaggedSettingUseCase()

    /**
     * Flow for the 'search across all tags when filtering' setting
     */
    val searchAcrossAllTagsWhenFiltering: Flow<Boolean> = getSearchAcrossAllTagsUseCase()

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
}
