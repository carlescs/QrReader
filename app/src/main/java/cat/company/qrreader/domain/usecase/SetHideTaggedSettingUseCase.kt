package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set hide tagged when no tag selected setting
 */
class SetHideTaggedSettingUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setHideTaggedWhenNoTagSelected(value)
    }
}

