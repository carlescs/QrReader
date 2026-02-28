package cat.company.qrreader.domain.repository

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for barcode operations
 */
interface BarcodeRepository {

    /**
     * Get all saved barcodes
     */
    fun getAllBarcodes(): Flow<List<BarcodeModel>>

    /**
     * Get barcodes with tags
     */
    fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>>

    /**
     * Get barcodes filtered by tag and search query
     */
    fun getBarcodesWithTagsByFilter(
        tagId: Int?,
        query: String?,
        hideTaggedWhenNoTagSelected: Boolean,
        searchAcrossAllTagsWhenFiltering: Boolean,
        showOnlyFavorites: Boolean = false
    ): Flow<List<BarcodeWithTagsModel>>

    /**
     * Insert barcodes
     */
    suspend fun insertBarcodes(vararg barcodes: BarcodeModel)
    
    /**
     * Insert a single barcode and return its ID
     */
    suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long

    /**
     * Update a barcode
     */
    suspend fun updateBarcode(barcode: BarcodeModel): Int

    /**
     * Delete a barcode
     */
    suspend fun deleteBarcode(barcode: BarcodeModel)

    /**
     * Add a tag to a barcode
     */
    suspend fun addTagToBarcode(barcodeId: Int, tagId: Int)

    /**
     * Remove a tag from a barcode
     */
    suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int)

    /**
     * Toggle the favorite state of a barcode
     */
    suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean)

    /**
     * Toggle the locked state of a barcode
     */
    suspend fun toggleLock(barcodeId: Int, isLocked: Boolean)

    /**
     * Get the count of locked barcodes
     */
    fun getLockedCount(): Flow<Int>

    /**
     * Toggle a tag on a barcode (add if not present, remove if present)
     */
    suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel)

    /**
     * Get barcode counts grouped by tag ID
     */
    fun getTagBarcodeCounts(): Flow<Map<Int, Int>>

    /**
     * Get the count of favorite barcodes
     */
    fun getFavoritesCount(): Flow<Int>
}
