package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get whether AI-generated features are enabled
 */
class GetAiGenerationEnabledUseCase(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<Boolean> {
        return settingsRepository.aiGenerationEnabled
    }
}
