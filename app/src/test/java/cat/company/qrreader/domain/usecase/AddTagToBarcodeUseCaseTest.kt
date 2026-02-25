package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.camera.AddTagToBarcodeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddTagToBarcodeUseCaseTest {

    @Test
    fun `adds tag to barcode via repository`() = runTest {
        var capturedBarcodeId: Int? = null
        var capturedTagId: Int? = null

        val repo = object : BarcodeRepository {
            override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
            override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override fun getBarcodesWithTagsByFilter(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean, searchAcrossAllTagsWhenFiltering: Boolean, showOnlyFavorites: Boolean): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
            override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}
            override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L
            override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
            override suspend fun deleteBarcode(barcode: BarcodeModel) {}
            override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {
                capturedBarcodeId = barcodeId
                capturedTagId = tagId
            }
            override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
            override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
            override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
        }

        val uc = AddTagToBarcodeUseCase(repo)
        uc(barcodeId = 42, tagId = 7)

        assertEquals(42, capturedBarcodeId)
        assertEquals(7, capturedTagId)
    }
}
