package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get whether AI-generated barcode descriptions should use a humorous tone
 */
class GetAiHumorousDescriptionsUseCase(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<Boolean> {
        return settingsRepository.aiHumorousDescriptions
    }
}
