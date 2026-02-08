package cat.company.qrreader.domain.model

/**
 * Domain model for a barcode with associated tags
 */
data class BarcodeWithTagsModel(
    val barcode: BarcodeModel,
    val tags: List<TagModel>
)

