package cat.company.qrreader.domain.usecase.camera

import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to add a tag to an already-saved barcode
 */
class AddTagToBarcodeUseCase(private val barcodeRepository: BarcodeRepository) {

    suspend operator fun invoke(barcodeId: Int, tagId: Int) {
        barcodeRepository.addTagToBarcode(barcodeId, tagId)
    }
}
