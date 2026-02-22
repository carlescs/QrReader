package cat.company.qrreader.data.mapper

import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.domain.model.BarcodeModel

/**
 * Mapper extension functions to convert between data and domain models
 */

// Entity to Domain Model
fun SavedBarcode.toDomainModel(): BarcodeModel {
    return BarcodeModel(
        id = this.id,
        date = this.date,
        type = this.type,
        format = this.format,
        title = this.title,
        description = this.description,
        barcode = this.barcode,
        aiGeneratedDescription = this.aiGeneratedDescription,
        isFavorite = this.isFavorite
    )
}

// Domain Model to Entity
fun BarcodeModel.toEntity(): SavedBarcode {
    return SavedBarcode(
        id = this.id,
        date = this.date,
        type = this.type,
        format = this.format,
        title = this.title,
        description = this.description,
        barcode = this.barcode,
        aiGeneratedDescription = this.aiGeneratedDescription,
        isFavorite = this.isFavorite
    )
}

