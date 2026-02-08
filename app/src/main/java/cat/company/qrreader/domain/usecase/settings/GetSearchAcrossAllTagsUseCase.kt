package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get the 'search across all tags when filtering' setting
 */
class GetSearchAcrossAllTagsUseCase(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<Boolean> {
        return settingsRepository.searchAcrossAllTagsWhenFiltering
    }
}

