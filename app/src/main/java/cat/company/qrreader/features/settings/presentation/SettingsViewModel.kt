package cat.company.qrreader.features.settings.presentation

import androidx.lifecycle.ViewModel
import cat.company.qrreader.domain.usecase.GetHideTaggedSettingUseCase
import cat.company.qrreader.domain.usecase.SetHideTaggedSettingUseCase
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
) : ViewModel() {

    /**
     * Flow of the hide tagged when no tag selected setting
     */
    val hideTaggedWhenNoTagSelected: Flow<Boolean> = getHideTaggedSettingUseCase()

    /**
     * Update the hide tagged when no tag selected setting
     */
    fun setHideTaggedWhenNoTagSelected(value: Boolean) {
        viewModelScope.launch {
            setHideTaggedSettingUseCase(value)
        }
    }
}

