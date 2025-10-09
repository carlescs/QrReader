package cat.company.qrreader.db.daos

import androidx.room.*
import cat.company.qrreader.db.entities.BarcodeTagCrossRef
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import kotlinx.coroutines.flow.Flow

/**
 * Dao for the saved barcodes
 */
@Dao
abstract class SavedBarcodeDao {
    @Query("SELECT * FROM saved_barcodes")
    abstract fun getAll(): Flow<List<SavedBarcode>>

    @Transaction
    @Query("SELECT * FROM saved_barcodes")
    abstract fun getSavedBarcodesWithTags(): Flow<List<SavedBarcodeWithTags>>

    @Transaction
    @Query("SELECT * FROM saved_barcodes WHERE :tagId is null or EXISTS (SELECT tagId FROM barcode_tag_cross_ref WHERE tagId = :tagId and barcodeId = saved_barcodes.id)")
    abstract fun getSavedBarcodesWithTagsByTagId(tagId: Int?): Flow<List<SavedBarcodeWithTags>>

    @Insert
    abstract suspend fun insertAll(vararg savedBarcodes: SavedBarcode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBarcodeTag(barcodeTag:BarcodeTagCrossRef)

    @Delete
    abstract suspend fun removeBarcodeTag(barcodeTag:BarcodeTagCrossRef)

    @Update
    abstract suspend fun updateItem(savedBarcode:SavedBarcode)
    @Delete
    abstract suspend fun delete(barcode:SavedBarcode)

    @Transaction
    open suspend fun switchTag(barcode: SavedBarcodeWithTags, tag: Tag){
        if(barcode.tags.contains(tag))
            removeBarcodeTag(BarcodeTagCrossRef(barcode.barcode.id, tag.id))
        else
            insertBarcodeTag(BarcodeTagCrossRef(barcode.barcode.id, tag.id))
    }
}