package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set the auto-lock on focus loss setting
 */
class SetAutoLockOnFocusLossUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setAutoLockOnFocusLoss(value)
    }
}
