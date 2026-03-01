package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set the show tag counters setting
 */
class SetShowTagCountersUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setShowTagCounters(value)
    }
}
