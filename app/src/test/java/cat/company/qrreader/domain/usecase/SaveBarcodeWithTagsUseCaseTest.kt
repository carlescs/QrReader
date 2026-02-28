package cat.company.qrreader.domain.usecase

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeWithTagsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class SaveBarcodeWithTagsUseCaseTest {

    private val baseBarcode = BarcodeModel(type = 1, format = 1, barcode = "TEST123", date = Date())

    // Fake repository that records the last inserted barcode and tag associations
    private class FakeBarcodeRepository(
        private val idToReturn: Long = 42L
    ) : BarcodeRepository {
        var lastInserted: BarcodeModel? = null
        val tagAssociations = mutableListOf<Pair<Int, Int>>() // barcodeId to tagId

        override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())
        override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())
        override fun getBarcodesWithTagsByFilter(
            tagId: Int?, query: String?,
            hideTaggedWhenNoTagSelected: Boolean,
            searchAcrossAllTagsWhenFiltering: Boolean,
            showOnlyFavorites: Boolean
        ): Flow<List<BarcodeWithTagsModel>> = flowOf(emptyList())

        override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {
            lastInserted = barcodes.firstOrNull()
        }

        override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long {
            lastInserted = barcode
            return idToReturn
        }

        override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0
        override suspend fun deleteBarcode(barcode: BarcodeModel) {}
        override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {
            tagAssociations.add(barcodeId to tagId)
        }
        override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}
        override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
        override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
        override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
        override fun getFavoritesCount(): Flow<Int> = flowOf(0)
    }

    @Test
    fun `save without description stores barcode without aiGeneratedDescription`() = runTest {
        val repo = FakeBarcodeRepository()
        val useCase = SaveBarcodeWithTagsUseCase(repo)

        useCase(baseBarcode, emptyList(), aiGeneratedDescription = null)

        assertNull(repo.lastInserted?.aiGeneratedDescription)
    }

    @Test
    fun `save with description stores barcode with aiGeneratedDescription`() = runTest {
        val repo = FakeBarcodeRepository()
        val useCase = SaveBarcodeWithTagsUseCase(repo)
        val description = "A QR code linking to example.com"

        useCase(baseBarcode, emptyList(), aiGeneratedDescription = description)

        assertEquals(description, repo.lastInserted?.aiGeneratedDescription)
    }

    @Test
    fun `save with tags associates all tags to the barcode id`() = runTest {
        val repo = FakeBarcodeRepository(idToReturn = 7L)
        val useCase = SaveBarcodeWithTagsUseCase(repo)
        val tags = listOf(
            TagModel(id = 1, name = "Shopping", color = "#FF0000"),
            TagModel(id = 2, name = "Online", color = "#00FF00")
        )

        useCase(baseBarcode, tags, aiGeneratedDescription = null)

        assertEquals(listOf(7 to 1, 7 to 2), repo.tagAssociations)
    }

    @Test
    fun `save with tags and description stores both`() = runTest {
        val repo = FakeBarcodeRepository(idToReturn = 3L)
        val useCase = SaveBarcodeWithTagsUseCase(repo)
        val description = "A product barcode"
        val tags = listOf(TagModel(id = 5, name = "Product", color = "#0000FF"))

        useCase(baseBarcode, tags, aiGeneratedDescription = description)

        assertEquals(description, repo.lastInserted?.aiGeneratedDescription)
        assertEquals(listOf(3 to 5), repo.tagAssociations)
    }

    @Test
    fun `save returns the id from the repository`() = runTest {
        val repo = FakeBarcodeRepository(idToReturn = 99L)
        val useCase = SaveBarcodeWithTagsUseCase(repo)

        val result = useCase(baseBarcode, emptyList())

        assertEquals(99L, result)
    }

    @Test
    fun `save without description does not overwrite existing description on barcode`() = runTest {
        val repo = FakeBarcodeRepository()
        val useCase = SaveBarcodeWithTagsUseCase(repo)
        val barcode = baseBarcode.copy(aiGeneratedDescription = "already set")

        useCase(barcode, emptyList(), aiGeneratedDescription = null)

        // When null is passed the barcode is saved as-is, preserving its own field
        assertEquals("already set", repo.lastInserted?.aiGeneratedDescription)
    }

    @Test
    fun `save with empty tags list associates no tags`() = runTest {
        val repo = FakeBarcodeRepository()
        val useCase = SaveBarcodeWithTagsUseCase(repo)

        useCase(baseBarcode, emptyList())

        assertEquals(emptyList<Pair<Int, Int>>(), repo.tagAssociations)
    }
}
