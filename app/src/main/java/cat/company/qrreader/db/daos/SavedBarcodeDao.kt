package cat.company.qrreader.db.daos

import androidx.room.*
import cat.company.qrreader.db.entities.BarcodeTagCrossRef
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import kotlinx.coroutines.flow.Flow

/**
 * Query projection holding barcode count for a single tag.
 *
 * @property tagId The tag identifier
 * @property count Number of barcodes associated with this tag
 */
data class TagBarcodeCount(val tagId: Int, val count: Int)

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
    @Query(
        """
        SELECT * FROM saved_barcodes
        WHERE (
            -- When searching across all tags with a query, bypass all filters except the search itself
            (:searchAcrossAllTagsWhenFiltering AND COALESCE(TRIM(:query), '') != '')
            OR (
                -- Tag filter: show all if no tag selected, or only those with the selected tag
                (:tagId IS NULL OR EXISTS (
                    SELECT 1 FROM barcode_tag_cross_ref
                    WHERE tagId = :tagId AND barcodeId = saved_barcodes.id
                ))
                AND (
                    -- Hide tagged items filter: only apply when flag is true, no tag selected, and not a "tagged item"
                    NOT :hideTaggedWhenNoTagSelected
                    OR :tagId IS NOT NULL
                    OR NOT EXISTS (
                        SELECT 1 FROM barcode_tag_cross_ref WHERE barcodeId = saved_barcodes.id
                    )
                )
            )
        )
        AND (
            -- Search filter: match query in title, description, or barcode
            COALESCE(TRIM(:query), '') = '' OR
            title LIKE '%' || TRIM(:query) || '%' COLLATE NOCASE OR
            description LIKE '%' || TRIM(:query) || '%' COLLATE NOCASE OR
            barcode LIKE '%' || TRIM(:query) || '%' COLLATE NOCASE
        )
        AND (
            -- Favorites filter: only show favorites when flag is true
            NOT :showOnlyFavorites OR is_favorite = 1
        )
        """
    )
    abstract fun getSavedBarcodesWithTagsByTagIdAndQuery(
        tagId: Int?,
        query: String?,
        hideTaggedWhenNoTagSelected: Boolean,
        searchAcrossAllTagsWhenFiltering: Boolean,
        showOnlyFavorites: Boolean
    ): Flow<List<SavedBarcodeWithTags>>

    @Query("UPDATE saved_barcodes SET is_favorite = :isFavorite WHERE id = :id")
    abstract suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE saved_barcodes SET is_locked = :isLocked WHERE id = :id")
    abstract suspend fun setLocked(id: Int, isLocked: Boolean)

    @Query("SELECT COUNT(*) FROM saved_barcodes WHERE is_locked = 1")
    abstract fun getLockedCount(): Flow<Int>

    @Insert
    abstract suspend fun insertAll(vararg savedBarcodes: SavedBarcode)
    
    @Insert
    abstract suspend fun insert(savedBarcode: SavedBarcode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBarcodeTag(barcodeTag:BarcodeTagCrossRef)

    @Delete
    abstract suspend fun removeBarcodeTag(barcodeTag:BarcodeTagCrossRef)

    @Update
    abstract suspend fun updateItem(savedBarcode:SavedBarcode): Int

    @Delete
    abstract suspend fun delete(barcode:SavedBarcode)

    @Query("SELECT tagId, COUNT(*) as count FROM barcode_tag_cross_ref GROUP BY tagId")
    abstract fun getTagBarcodeCounts(): Flow<List<TagBarcodeCount>>

    @Query("SELECT COUNT(*) FROM saved_barcodes WHERE is_favorite = 1")
    abstract fun getFavoritesCount(): Flow<Int>

    @Query("SELECT * FROM saved_barcodes WHERE barcode = :content COLLATE NOCASE LIMIT 1")
    abstract suspend fun findByContent(content: String): SavedBarcode?

    @Transaction
    open suspend fun switchTag(barcode: SavedBarcodeWithTags, tag: Tag){
        if(barcode.tags.contains(tag))
            removeBarcodeTag(BarcodeTagCrossRef(barcode.barcode.id, tag.id))
        else
            insertBarcodeTag(BarcodeTagCrossRef(barcode.barcode.id, tag.id))
    }
}