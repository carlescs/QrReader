package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to delete a barcode
 */
class DeleteBarcodeUseCase(private val barcodeRepository: BarcodeRepository) {

    suspend operator fun invoke(barcode: BarcodeModel) {
        barcodeRepository.deleteBarcode(barcode)
    }
}

