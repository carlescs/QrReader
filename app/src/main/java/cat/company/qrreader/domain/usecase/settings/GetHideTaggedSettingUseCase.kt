package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get hide tagged when no tag selected setting
 */
class GetHideTaggedSettingUseCase(private val settingsRepository: SettingsRepository) {

    operator fun invoke(): Flow<Boolean> {
        return settingsRepository.hideTaggedWhenNoTagSelected
    }
}

