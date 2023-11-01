package cat.company.qrreader.db.entities.compound

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import cat.company.qrreader.db.entities.BarcodeTagCrossRef
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag

data class SavedBarcodeWithTags(
    @Embedded
    val barcode: SavedBarcode,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(BarcodeTagCrossRef::class,
            parentColumn = "barcodeId",
            entityColumn = "tagId")
    )
    val tags: List<Tag> = emptyList())
