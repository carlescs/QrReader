package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class SaveBarcodeUseCaseTest {
    @Test
    fun `inserts barcode via repository`() = runTest {
        val barcode = BarcodeModel(id = 9, date = Date(), type = 1, format = 1, barcode = "Z")
        var inserted: BarcodeModel? = null
        val repo = object : BarcodeRepository {
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean, showOnlyFavorites: Boolean): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) { inserted = barcodes.firstOrNull() }
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
            override suspend fun toggleLock(barcodeId: Int, isLocked: Boolean) {}
            override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
            override fun getFavoritesCount(): Flow<Int> = flowOf(0)
            override fun getLockedCount(): Flow<Int> = flowOf(0)
        }

        val uc = SaveBarcodeUseCase(repo)
        uc(barcode)
        assertEquals(barcode, inserted)
    }
}

