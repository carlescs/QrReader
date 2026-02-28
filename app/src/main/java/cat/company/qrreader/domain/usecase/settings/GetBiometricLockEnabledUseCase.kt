package cat.company.qrreader.domain.usecase.settings

import cat.company.qrreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get the biometric lock enabled setting
 */
class GetBiometricLockEnabledUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(): Flow<Boolean> = settingsRepository.biometricLockEnabled
}
