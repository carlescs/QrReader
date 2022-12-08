package cat.company.qrreader.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import cat.company.qrreader.db.entities.SavedBarcode
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBarcodeDao {
    @Query("SELECT * FROM saved_barcodes")
    fun getAll(): Flow<List<SavedBarcode>>

    fun insertAll(vararg savedBarcodes: SavedBarcode)

    @Delete
    fun delete(barcode:SavedBarcode)
}