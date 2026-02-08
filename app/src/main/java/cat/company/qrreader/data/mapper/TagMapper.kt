package cat.company.qrreader.data.mapper

import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.domain.model.TagModel

/**
 * Mapper extension functions to convert between data and domain models for tags
 */

// Entity to Domain Model
fun Tag.toDomainModel(): TagModel {
    return TagModel(
        id = this.id,
        name = this.name,
        color = this.color
    )
}

// Domain Model to Entity
fun TagModel.toEntity(): Tag {
    return Tag(
        id = this.id,
        name = this.name,
        color = this.color
    )
}

