package cat.company.qrreader.data.mapper

import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import cat.company.qrreader.domain.model.BarcodeWithTagsModel

/**
 * Mapper extension functions for barcode with tags compound objects
 */

fun SavedBarcodeWithTags.toDomainModel(): BarcodeWithTagsModel {
    return BarcodeWithTagsModel(
        barcode = this.barcode.toDomainModel(),
        tags = this.tags.map { it.toDomainModel() }
    )
}

