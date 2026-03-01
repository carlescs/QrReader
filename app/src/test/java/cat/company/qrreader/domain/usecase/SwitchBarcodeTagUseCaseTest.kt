package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.history.SwitchBarcodeTagUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class SwitchBarcodeTagUseCaseTest {
    @Test
    fun `toggles tag via repository`() = runTest {
        val tag = TagModel(5, "t", "#fff")
        var switchedPair: Pair<BarcodeWithTagsModel, TagModel>? = null
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
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) { switchedPair = Pair(barcode, tag) }
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
            override suspend fun toggleLock(barcodeId: Int, isLocked: Boolean) {}
            override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
            override fun getFavoritesCount(): Flow<Int> = flowOf(0)
            override fun getLockedCount(): Flow<Int> = flowOf(0)
        }

        val barcode = BarcodeWithTagsModel(BarcodeModel(id = 1, date = Date(), type = 1, format = 1, barcode = "val"), emptyList())
        val uc = SwitchBarcodeTagUseCase(repo)
        uc(barcode, tag)
        assertEquals(barcode, switchedPair?.first)
        assertEquals(tag, switchedPair?.second)
    }
}

