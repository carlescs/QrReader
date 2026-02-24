package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set whether AI-generated barcode descriptions should use a humorous tone
 */
class SetAiHumorousDescriptionsUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setAiHumorousDescriptions(value)
    }
}
