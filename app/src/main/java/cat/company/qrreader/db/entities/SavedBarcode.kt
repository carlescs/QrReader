package cat.company.qrreader.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "saved_barcodes")
data class SavedBarcode(
    @PrimaryKey(autoGenerate = true) val id:Int=0,
    val date: Date = Date(),
    val type:Int,
    val format: Int,
    val barcode: String
)
