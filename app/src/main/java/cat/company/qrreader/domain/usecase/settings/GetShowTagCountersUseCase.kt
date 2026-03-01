package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get the show tag counters setting
 */
class GetShowTagCountersUseCase(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<Boolean> {
        return settingsRepository.showTagCounters
    }
}
