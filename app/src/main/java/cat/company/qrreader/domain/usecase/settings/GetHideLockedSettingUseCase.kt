package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get the hide locked barcodes when not in Safe section setting
 */
class GetHideLockedSettingUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(): Flow<Boolean> = settingsRepository.hideLockedWhenNotInSafe
}
