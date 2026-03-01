package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set the app-level lock enabled setting
 */
class SetAppLockEnabledUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setAppLockEnabled(value)
    }
}
