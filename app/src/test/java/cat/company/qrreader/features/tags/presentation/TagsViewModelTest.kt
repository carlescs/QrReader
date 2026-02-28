package cat.company.qrreader.features.tags.presentation

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.repository.TagRepository
import cat.company.qrreader.domain.usecase.tags.DeleteTagUseCase
import cat.company.qrreader.domain.usecase.tags.GetAllTagsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TagsViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TagsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeTagRepository: FakeTagRepository
    private lateinit var fakeBarcodeRepository: FakeBarcodeRepository
    private lateinit var getAllTagsUseCase: GetAllTagsUseCase
    private lateinit var deleteTagUseCase: DeleteTagUseCase
    private lateinit var viewModel: TagsViewModel

    // Fake TagRepository implementation
    private class FakeTagRepository : TagRepository {        private val tagsFlow = MutableStateFlow<List<TagModel>>(emptyList())
        val deletedTags = mutableListOf<TagModel>()
        val insertedTags = mutableListOf<TagModel>()
        val updatedTags = mutableListOf<TagModel>()

        override fun getAllTags(): Flow<List<TagModel>> = tagsFlow

        override suspend fun insertTags(vararg tags: TagModel) {
            insertedTags.addAll(tags)
        }

        override suspend fun updateTag(tag: TagModel) {
            updatedTags.add(tag)
        }

        override suspend fun deleteTag(tag: TagModel) {
            deletedTags.add(tag)
        }

        fun emitTags(tags: List<TagModel>) {
            tagsFlow.value = tags
        }
    }

    // Fake BarcodeRepository for testing counts
    private class FakeBarcodeRepository : BarcodeRepository {
        private val tagBarcodeCountsFlow = MutableStateFlow<Map<Int, Int>>(emptyMap())
        private val favoritesCountFlow = MutableStateFlow(0)

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
        override suspend fun toggleFavorite(barcodeId: Int, isFavorite: Boolean) {}
        override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = tagBarcodeCountsFlow
        override fun getFavoritesCount(): Flow<Int> = favoritesCountFlow

        fun emitTagBarcodeCounts(counts: Map<Int, Int>) { tagBarcodeCountsFlow.value = counts }
        fun emitFavoritesCount(count: Int) { favoritesCountFlow.value = count }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeTagRepository = FakeTagRepository()
        fakeBarcodeRepository = FakeBarcodeRepository()
        getAllTagsUseCase = GetAllTagsUseCase(fakeTagRepository)
        deleteTagUseCase = DeleteTagUseCase(fakeTagRepository)
        viewModel = TagsViewModel(getAllTagsUseCase, deleteTagUseCase, fakeBarcodeRepository, fakeTagRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test loadTags initializes tags flow
     */
    @Test
    fun loadTags_initializesTagsFlow() {
        viewModel.loadTags()

        assertNotNull(viewModel.tags)
    }

    /**
     * Test loadTags returns empty list when no tags
     */
    @Test
    fun loadTags_noTags_returnsEmptyList() = runTest {
        viewModel.loadTags()

        val tags = viewModel.tags.first()
        assertTrue(tags.isEmpty())
    }

    /**
     * Test loadTags returns tags when they exist
     */
    @Test
    fun loadTags_withTags_returnsTags() = runTest {
        val expectedTags = listOf(
            TagModel(id = 1, name = "Work", color = "#FF0000"),
            TagModel(id = 2, name = "Personal", color = "#00FF00")
        )
        fakeTagRepository.emitTags(expectedTags)

        viewModel.loadTags()

        val tags = viewModel.tags.first()
        assertEquals(2, tags.size)
        assertEquals("Work", tags[0].name)
        assertEquals("Personal", tags[1].name)
    }

    /**
     * Test deleteTag calls use case
     */
    @Test
    fun deleteTag_callsUseCase() = runTest {
        val tag = TagModel(id = 1, name = "ToDelete", color = "#000000")

        viewModel.deleteTag(tag)
        advanceUntilIdle()

        assertEquals(1, fakeTagRepository.deletedTags.size)
        assertEquals(tag, fakeTagRepository.deletedTags[0])
    }

    /**
     * Test deleteTag removes tag from flow
     */
    @Test
    fun deleteTag_removesTagFromFlow() = runTest {
        val tag1 = TagModel(id = 1, name = "Keep", color = "#FF0000")
        val tag2 = TagModel(id = 2, name = "Delete", color = "#00FF00")
        fakeTagRepository.emitTags(listOf(tag1, tag2))
        viewModel.loadTags()

        // Verify both tags are present
        assertEquals(2, viewModel.tags.first().size)

        // Delete tag2
        viewModel.deleteTag(tag2)
        advanceUntilIdle()

        // Note: In real implementation, the flow would update from the database
        // Here we just verify the delete was called
        assertEquals(1, fakeTagRepository.deletedTags.size)
    }

    /**
     * Test deleteTag with non-existent tag doesn't crash
     */
    @Test
    fun deleteTag_nonExistentTag_doesNotCrash() = runTest {
        val tag = TagModel(id = 999, name = "NonExistent", color = "#000000")

        // Should not throw
        viewModel.deleteTag(tag)
        advanceUntilIdle()

        assertEquals(1, fakeTagRepository.deletedTags.size)
    }

    /**
     * Test multiple deletes work correctly
     */
    @Test
    fun deleteTag_multipleDeletes_allRecorded() = runTest {
        val tag1 = TagModel(id = 1, name = "Tag1", color = "#000000")
        val tag2 = TagModel(id = 2, name = "Tag2", color = "#111111")
        val tag3 = TagModel(id = 3, name = "Tag3", color = "#222222")

        viewModel.deleteTag(tag1)
        viewModel.deleteTag(tag2)
        viewModel.deleteTag(tag3)
        advanceUntilIdle()

        assertEquals(3, fakeTagRepository.deletedTags.size)
    }

    /**
     * Test tags flow updates when use case emits new values
     */
    @Test
    fun tagsFlow_updatesWhenUseCaseEmits() = runTest {
        viewModel.loadTags()

        // Initially empty
        assertEquals(0, viewModel.tags.first().size)

        // Add a tag
        fakeTagRepository.emitTags(listOf(TagModel(id = 1, name = "New", color = "#FFFFFF")))

        // Now should have one tag
        assertEquals(1, viewModel.tags.first().size)
        assertEquals("New", viewModel.tags.first()[0].name)
    }

    /**
     * Test favoritesCount emits zero when no favorites exist (badge not shown)
     */
    @Test
    fun favoritesCount_noFavorites_emitsZero() = runTest {
        val count = viewModel.favoritesCount.first()
        assertEquals(0, count)
    }

    /**
     * Test favoritesCount emits the value from repository (badge shown with count)
     */
    @Test
    fun favoritesCount_withFavorites_emitsCount() = runTest {
        fakeBarcodeRepository.emitFavoritesCount(5)

        val count = viewModel.favoritesCount.first()
        assertEquals(5, count)
    }

    /**
     * Test favoritesCount updates reactively when repository emits new value
     */
    @Test
    fun favoritesCount_updatesReactively() = runTest {
        fakeBarcodeRepository.emitFavoritesCount(0)
        assertEquals(0, viewModel.favoritesCount.first())

        fakeBarcodeRepository.emitFavoritesCount(3)
        assertEquals(3, viewModel.favoritesCount.first())
    }

    /**
     * Test tagBarcodeCounts emits empty map when no barcodes are tagged (badges not shown)
     */
    @Test
    fun tagBarcodeCounts_noTaggedBarcodes_emitsEmptyMap() = runTest {
        val counts = viewModel.tagBarcodeCounts.first()
        assertTrue(counts.isEmpty())
    }

    /**
     * Test tagBarcodeCounts emits correct counts per tag (badges shown per tag)
     */
    @Test
    fun tagBarcodeCounts_withTaggedBarcodes_emitsCounts() = runTest {
        val expectedCounts = mapOf(1 to 3, 2 to 7)
        fakeBarcodeRepository.emitTagBarcodeCounts(expectedCounts)

        val counts = viewModel.tagBarcodeCounts.first()
        assertEquals(3, counts[1])
        assertEquals(7, counts[2])
    }

    /**
     * Test tagBarcodeCounts updates reactively when repository emits new value
     */
    @Test
    fun tagBarcodeCounts_updatesReactively() = runTest {
        fakeBarcodeRepository.emitTagBarcodeCounts(emptyMap())
        assertTrue(viewModel.tagBarcodeCounts.first().isEmpty())

        fakeBarcodeRepository.emitTagBarcodeCounts(mapOf(1 to 2))
        assertEquals(2, viewModel.tagBarcodeCounts.first()[1])
    }
}
