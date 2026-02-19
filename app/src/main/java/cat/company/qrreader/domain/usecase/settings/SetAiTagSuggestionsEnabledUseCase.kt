package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to enable or disable AI-generated tag suggestions
 */
class SetAiTagSuggestionsEnabledUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setAiTagSuggestionsEnabled(value)
    }
}
