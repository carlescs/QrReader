package cat.company.qrreader.domain.usecase.history

import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to toggle the lock state of a barcode
 */
class ToggleLockBarcodeUseCase(private val barcodeRepository: BarcodeRepository) {
    suspend operator fun invoke(barcodeId: Int, isLocked: Boolean) {
        barcodeRepository.toggleLock(barcodeId, isLocked)
    }
}
