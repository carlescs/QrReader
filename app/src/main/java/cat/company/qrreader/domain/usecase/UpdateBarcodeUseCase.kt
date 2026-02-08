package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to update a barcode
 */
class UpdateBarcodeUseCase(private val barcodeRepository: BarcodeRepository) {

    suspend operator fun invoke(barcode: BarcodeModel): Int {
        return barcodeRepository.updateBarcode(barcode)
    }
}

