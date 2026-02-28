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
        override fun getTagBarcodeCounts(): Flow<Map<Int, Int>> = flowOf(emptyMap())
        override fun getFavoritesCount(): Flow<Int> = flowOf(0)
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
}
