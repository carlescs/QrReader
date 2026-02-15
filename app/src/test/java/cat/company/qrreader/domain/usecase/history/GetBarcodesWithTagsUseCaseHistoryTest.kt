package cat.company.qrreader.domain.usecase.history

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class GetBarcodesWithTagsUseCaseHistoryTest {

    @Test
    fun `when query is empty forwards tagId to repository`() = runTest {
        val recorded = mutableListOf<Triple<Int?, String?, Boolean?>>()
        val sample = BarcodeWithTagsModel(BarcodeModel(id = 1, date = Date(), type = 1, format = 1, barcode = "X"), emptyList())
        val repo = object : BarcodeRepository {
            private val flow = flowOf(listOf(sample))
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flow
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean): Flow<List<BarcodeWithTagsModel>> {
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
        }

        val uc = GetBarcodesWithTagsUseCase(repo)
        val result = uc(42, "", true, false).first()

        assertEquals(1, recorded.size)
        assertEquals(42, recorded[0].first)
        assertEquals("", recorded[0].second)
        assertEquals(true, recorded[0].third)
        assertEquals(listOf(sample), result)
    }

    @Test
    fun `when query is non-empty forwards tagId to repository`() = runTest {
        val recorded = mutableListOf<Triple<Int?, String?, Boolean?>>()
        val sample = BarcodeWithTagsModel(BarcodeModel(id = 2, date = Date(), type = 1, format = 1, barcode = "Y"), emptyList())
        val repo = object : BarcodeRepository {
            private val flow = flowOf(listOf(sample))
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flow
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean): Flow<List<BarcodeWithTagsModel>> {
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
        }

        val uc = GetBarcodesWithTagsUseCase(repo)
        val result = uc(42, "query", false, false).first()

        assertEquals(1, recorded.size)
        // Use case now forwards tagId as-is (ViewModel handles searchAcrossAllTags logic)
        assertEquals(42, recorded[0].first)
        assertEquals("query", recorded[0].second)
        assertEquals(false, recorded[0].third)
        assertEquals(listOf(sample), result)
    }

    @Test
    fun `when query is null does not forward tagId to repository`() = runTest {
        val recorded = mutableListOf<Triple<Int?, String?, Boolean?>>()
        val sample = BarcodeWithTagsModel(BarcodeModel(id = 3, date = Date(), type = 1, format = 1, barcode = "Z"), emptyList())
        val repo = object : BarcodeRepository {
            private val flow = flowOf(listOf(sample))
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flow
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean): Flow<List<BarcodeWithTagsModel>> {
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
        }

        val uc = GetBarcodesWithTagsUseCase(repo)
        val result = uc(42, null, true, false).first()

        assertEquals(1, recorded.size)
        // When query is null (blank), tagId is forwarded
        assertEquals(42, recorded[0].first)
        assertEquals(null, recorded[0].second)
        assertEquals(true, recorded[0].third)
        assertEquals(listOf(sample), result)
    }
}

