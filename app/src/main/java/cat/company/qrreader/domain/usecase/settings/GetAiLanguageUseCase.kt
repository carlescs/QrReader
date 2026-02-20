package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get the configured language for AI-generated texts and tags
 */
class GetAiLanguageUseCase(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<String> {
        return settingsRepository.aiLanguage
    }
}
