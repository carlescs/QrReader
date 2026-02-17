package cat.company.qrreader.domain.usecase.camera

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository

/**
 * Use case to save a barcode with associated tags
 */
class SaveBarcodeWithTagsUseCase(
    private val barcodeRepository: BarcodeRepository
) {
    /**
     * Save a barcode and associate it with the provided tags
     * @param barcode The barcode to save
     * @param tags The tags to associate with the barcode
     * @return The ID of the saved barcode
     */
    suspend operator fun invoke(barcode: BarcodeModel, tags: List<TagModel>): Long {
        // Insert the barcode and get its ID
        val barcodeId = barcodeRepository.insertBarcodeAndGetId(barcode)
        
        // Add tags to the barcode
        tags.forEach { tag ->
            barcodeRepository.addTagToBarcode(barcodeId.toInt(), tag.id)
        }
        
        return barcodeId
    }
}
