package cat.company.qrreader.db.entities

import androidx.room.Entity

/**
 * Entity for the barcode tag cross reference
 */
@Entity(tableName = "barcode_tag_cross_ref",
    primaryKeys = ["barcodeId", "tagId"],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = SavedBarcode::class,
            parentColumns = ["id"],
            childColumns = ["barcodeId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index("barcodeId"),
        androidx.room.Index("tagId")
    ])
data class BarcodeTagCrossRef(
    val barcodeId: Int,
    val tagId: Int
)
