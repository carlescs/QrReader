package cat.company.qrreader.data.repository

import cat.company.qrreader.data.mapper.toDomainModel
import cat.company.qrreader.data.mapper.toEntity
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.BarcodeTagCrossRef
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of BarcodeRepository using Room database
 */
class BarcodeRepositoryImpl(database: BarcodesDb) : BarcodeRepository {

    private val barcodeDao = database.savedBarcodeDao()

    override fun getAllBarcodes(): Flow<List<BarcodeModel>> {
        return barcodeDao.getAll().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> {
        return barcodeDao.getSavedBarcodesWithTags().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getBarcodesWithTagsByFilter(
        tagId: Int?,
        query: String?,
        hideTaggedWhenNoTagSelected: Boolean,
        searchAcrossAllTagsWhenFiltering: Boolean,
        showOnlyFavorites: Boolean
    ): Flow<List<BarcodeWithTagsModel>> {
        return barcodeDao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = tagId,
            query = query,
            hideTaggedWhenNoTagSelected = hideTaggedWhenNoTagSelected,
            searchAcrossAllTagsWhenFiltering = searchAcrossAllTagsWhenFiltering,
            showOnlyFavorites = showOnlyFavorites
        ).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {
        barcodeDao.insertAll(*barcodes.map { it.toEntity() }.toTypedArray())
    }
    
    override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long {
        return barcodeDao.insert(barcode.toEntity())
    }

    override suspend fun updateBarcode(barcode: BarcodeModel): Int {
        return barcodeDao.updateItem(barcode.toEntity())
    }

    override suspend fun deleteBarcode(barcode: BarcodeModel) {
        barcodeDao.delete(barcode.toEntity())
    }

    override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {
        barcodeDao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId, tagId))
    }

    override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {
        barcodeDao.removeBarcodeTag(BarcodeTagCrossRef(barcodeId, tagId))
    }

    override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {
        if (barcode.tags.contains(tag)) {
            removeTagFromBarcode(barcode.barcode.id, tag.id)
        } else {
            addTagToBarcode(barcode.barcode.id, tag.id)
        }
    }

    override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {
        barcodeDao.setFavorite(barcodeId, isFavorite)
    }

    override suspend fun toggleLock(barcodeId: Int, isLocked: Boolean) {
        barcodeDao.setLocked(barcodeId, isLocked)
    }

    override fun getLockedCount(): Flow<Int> = barcodeDao.getLockedCount()

    override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> {
        return barcodeDao.getTagBarcodeCounts().map { list ->
            list.associate { it.tagId to it.count }
        }
    }

    override fun getFavoritesCount(): Flow<Int> {
        return barcodeDao.getFavoritesCount()
    }

    override suspend fun findByContent(content: String): BarcodeModel? {
        return barcodeDao.findByContent(content)?.toDomainModel()
    }
}

