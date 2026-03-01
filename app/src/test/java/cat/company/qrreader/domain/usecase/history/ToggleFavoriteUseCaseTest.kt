package cat.company.qrreader.domain.usecase.history

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class ToggleFavoriteUseCaseTest {

    @Test
    fun `sets favorite true via repository`() = runTest {
        var lastId: Int? = null
        var lastIsFavorite: Boolean? = null
        val repo = object : BarcodeRepository {
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean, showOnlyFavorites: Boolean): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {
                lastId = barcodeId
                lastIsFavorite = isFavorite
            }
            override suspend fun toggleLock(barcodeId: Int, isLocked: Boolean) {}
            override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
            override fun getFavoritesCount(): Flow<Int> = flowOf(0)
            override fun getLockedCount(): Flow<Int> = flowOf(0)
            override suspend fun findByContent(content: String): BarcodeModel? = null
        }

        val barcode = BarcodeModel(id = 7, date = Date(), type = 1, format = 1, barcode = "TEST")
        val uc = ToggleFavoriteUseCase(repo)
        uc(barcode.id, true)

        assertEquals(7, lastId)
        assertEquals(true, lastIsFavorite)
    }

    @Test
    fun `sets favorite false via repository`() = runTest {
        var lastIsFavorite: Boolean? = null
        val repo = object : BarcodeRepository {
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean, showOnlyFavorites: Boolean): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {
                lastIsFavorite = isFavorite
            }
            override suspend fun toggleLock(barcodeId: Int, isLocked: Boolean) {}
            override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
            override fun getFavoritesCount(): Flow<Int> = flowOf(0)
            override fun getLockedCount(): Flow<Int> = flowOf(0)
            override suspend fun findByContent(content: String): BarcodeModel? = null
        }

        val uc = ToggleFavoriteUseCase(repo)
        uc(3, false)

        assertEquals(false, lastIsFavorite)
    }
}
