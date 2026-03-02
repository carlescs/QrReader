package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set the hide locked barcodes when not in Safe section setting
 */
class SetHideLockedSettingUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setHideLockedWhenNotInSafe(value)
    }
}
