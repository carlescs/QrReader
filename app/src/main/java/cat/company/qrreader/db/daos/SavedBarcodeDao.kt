package cat.company.qrreader.db.daos

import androidx.room.*
import cat.company.qrreader.db.entities.SavedBarcode
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBarcodeDao {
    @Query("SELECT * FROM saved_barcodes")
    fun getAll(): Flow<List<SavedBarcode>>

    @Insert
    fun insertAll(vararg savedBarcodes: SavedBarcode)

    @Update
    fun updateItem(savedBarcode:SavedBarcode)

    @Delete
    fun delete(barcode:SavedBarcode)
}