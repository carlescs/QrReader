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
        searchAcrossAllTagsWhenFiltering: Boolean
    ): Flow<List<BarcodeWithTagsModel>>

    /**
     * Insert barcodes
     */
    suspend fun insertBarcodes(vararg barcodes: BarcodeModel)

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
     * Toggle a tag on a barcode (add if not present, remove if present)
     */
    suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel)
}

