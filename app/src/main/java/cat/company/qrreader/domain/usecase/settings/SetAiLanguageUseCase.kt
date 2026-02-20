package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set the language for AI-generated texts and tags
 */
class SetAiLanguageUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(value: String) {
        settingsRepository.setAiLanguage(value)
    }
}
