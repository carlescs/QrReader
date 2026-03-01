package cat.company.qrreader.domain.usecase.camera

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to check if a barcode with the given content already exists in history.
 * The comparison is case-insensitive.
 *
 * @return The existing [BarcodeModel] if a duplicate is found, or null otherwise.
 */
class CheckDuplicateBarcodeUseCase(
    private val barcodeRepository: BarcodeRepository
) {
    suspend operator fun invoke(content: String): BarcodeModel? =
        barcodeRepository.findByContent(content)
}
