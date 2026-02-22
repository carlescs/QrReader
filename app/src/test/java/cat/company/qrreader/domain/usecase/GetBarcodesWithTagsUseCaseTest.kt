package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class GetBarcodesWithTagsUseCaseTest {

    @Test
    fun `forwards repository flow and parameters`() = runTest {
        val recorded = mutableListOf<Triple<Int?, String?, Boolean?>>()
        val sample = BarcodeWithTagsModel(BarcodeModel(id = 1, date = Date(), type = 1, format = 1, barcode = "X"), emptyList())
        val repo = object : BarcodeRepository {
            private val flow = flowOf(listOf(sample))
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flow
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean, showOnlyFavorites: Boolean): Flow<List<BarcodeWithTagsModel>> {
                recorded.add(Triple(tagId, query, hideTaggedWhenNoTagSelected))
                return flow
            }
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
        }

        val uc = GetBarcodesWithTagsUseCase(repo)
        val result = uc(42, "q", true, false).first()

        assertEquals(1, recorded.size)
        // Use case now forwards tagId as-is (ViewModel handles searchAcrossAllTags logic)
        assertEquals(42, recorded[0].first)
        assertEquals("q", recorded[0].second)
        assertEquals(true, recorded[0].third)

        assertEquals(listOf(sample), result)
    }
}

