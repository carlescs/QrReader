package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to toggle a tag on a barcode
 */
class SwitchBarcodeTagUseCase(private val barcodeRepository: BarcodeRepository) {

    suspend operator fun invoke(barcode: BarcodeWithTagsModel, tag: TagModel) {
        barcodeRepository.switchTag(barcode, tag)
    }
}

