package cat.company.qrreader.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity for the saved barcode
 */
@Entity(tableName = "saved_barcodes")
data class SavedBarcode(
    @PrimaryKey(autoGenerate = true) val id:Int=0,
    val date: Date = Date(),
    val type:Int,
    val format: Int,
    var title: String?=null,
    var description: String?=null,
    val barcode: String,
    var aiGeneratedDescription: String?=null,
)
