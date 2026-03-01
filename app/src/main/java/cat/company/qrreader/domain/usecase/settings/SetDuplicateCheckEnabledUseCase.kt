package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set the duplicate check enabled setting
 */
class SetDuplicateCheckEnabledUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) = settingsRepository.setDuplicateCheckEnabled(value)
}
