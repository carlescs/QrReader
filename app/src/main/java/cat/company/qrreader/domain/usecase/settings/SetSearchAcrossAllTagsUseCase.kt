package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository

/**
 * Use case to set the 'search across all tags when filtering' setting
 */
class SetSearchAcrossAllTagsUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(value: Boolean) {
        settingsRepository.setSearchAcrossAllTagsWhenFiltering(value)
    }
}

