package cat.company.qrreader.history.tags

import androidx.room.InvalidationTracker
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.daos.SavedBarcodeDao
import cat.company.qrreader.db.daos.TagDao
import cat.company.qrreader.db.entities.BarcodeTagCrossRef
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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

    private lateinit var fakeTagDao: FakeTagDao
    private lateinit var fakeDb: FakeBarcodesDb
    private lateinit var viewModel: TagsViewModel

    // Fake TagDao implementation
    private class FakeTagDao : TagDao {
        private val tagsFlow = MutableStateFlow<List<Tag>>(emptyList())
        val deletedTags = mutableListOf<Tag>()
        val insertedTags = mutableListOf<Tag>()
        val updatedTags = mutableListOf<Tag>()

        override fun getAll(): Flow<List<Tag>> = tagsFlow

        override fun insertAll(vararg tags: Tag) {
            insertedTags.addAll(tags)
            tagsFlow.value = tagsFlow.value + tags
        }

        override fun updateItem(tag: Tag) {
            updatedTags.add(tag)
        }

        override fun delete(tag: Tag) {
            deletedTags.add(tag)
            tagsFlow.value = tagsFlow.value.filter { it.id != tag.id }
        }

        fun emitTags(tags: List<Tag>) {
            tagsFlow.value = tags
        }
    }

    // Minimal fake SavedBarcodeDao
    private class FakeSavedBarcodeDao : SavedBarcodeDao() {
        override fun getAll(): Flow<List<SavedBarcode>> = flowOf(emptyList())
        override fun getSavedBarcodesWithTags(): Flow<List<SavedBarcodeWithTags>> = flowOf(emptyList())
        override fun getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId: Int?,
            query: String?,
            hideTaggedWhenNoTagSelected: Boolean
        ): Flow<List<SavedBarcodeWithTags>> = flowOf(emptyList())

        override suspend fun insertAll(vararg savedBarcodes: SavedBarcode) {}
        override suspend fun insertBarcodeTag(barcodeTag: BarcodeTagCrossRef) {}
        override suspend fun removeBarcodeTag(barcodeTag: BarcodeTagCrossRef) {}
        override suspend fun updateItem(savedBarcode: SavedBarcode): Int = 0
        override suspend fun delete(barcode: SavedBarcode) {}
    }

    // Fake BarcodesDb
    private class FakeBarcodesDb(private val tagDao: TagDao) : BarcodesDb() {
        private val savedBarcodeDao = FakeSavedBarcodeDao()

        override fun savedBarcodeDao(): SavedBarcodeDao = savedBarcodeDao
        override fun tagDao(): TagDao = tagDao

        override fun createInvalidationTracker(): InvalidationTracker {
            throw UnsupportedOperationException("Not needed for unit tests")
        }

        override fun clearAllTables() {
            throw UnsupportedOperationException("Not needed for unit tests")
        }
    }

    @Before
    fun setup() {
        fakeTagDao = FakeTagDao()
        fakeDb = FakeBarcodesDb(fakeTagDao)
        viewModel = TagsViewModel(fakeDb)
    }

    /**
     * Test ViewModel is created with db reference
     */
    @Test
    fun viewModel_createdWithDb_hasDbReference() {
        assertNotNull(viewModel.db)
        assertEquals(fakeDb, viewModel.db)
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
            Tag(id = 1, name = "Work", color = "#FF0000"),
            Tag(id = 2, name = "Personal", color = "#00FF00")
        )
        fakeTagDao.emitTags(expectedTags)

        viewModel.loadTags()

        val tags = viewModel.tags.first()
        assertEquals(2, tags.size)
        assertEquals("Work", tags[0].name)
        assertEquals("Personal", tags[1].name)
    }

    /**
     * Test deleteTag calls dao delete
     */
    @Test
    fun deleteTag_callsDaoDelete() {
        val tag = Tag(id = 1, name = "ToDelete", color = "#000000")

        viewModel.deleteTag(tag)

        assertEquals(1, fakeTagDao.deletedTags.size)
        assertEquals(tag, fakeTagDao.deletedTags[0])
    }

    /**
     * Test deleteTag removes tag from flow
     */
    @Test
    fun deleteTag_removesTagFromFlow() = runTest {
        val tag1 = Tag(id = 1, name = "Keep", color = "#FF0000")
        val tag2 = Tag(id = 2, name = "Delete", color = "#00FF00")
        fakeTagDao.emitTags(listOf(tag1, tag2))
        viewModel.loadTags()

        // Verify both tags are present
        assertEquals(2, viewModel.tags.first().size)

        // Delete tag2
        viewModel.deleteTag(tag2)

        // Verify only tag1 remains
        val remainingTags = viewModel.tags.first()
        assertEquals(1, remainingTags.size)
        assertEquals("Keep", remainingTags[0].name)
    }

    /**
     * Test deleteTag with non-existent tag doesn't crash
     */
    @Test
    fun deleteTag_nonExistentTag_doesNotCrash() {
        val tag = Tag(id = 999, name = "NonExistent", color = "#000000")

        // Should not throw
        viewModel.deleteTag(tag)

        assertEquals(1, fakeTagDao.deletedTags.size)
    }

    /**
     * Test multiple deletes work correctly
     */
    @Test
    fun deleteTag_multipleDeletes_allRecorded() {
        val tag1 = Tag(id = 1, name = "Tag1", color = "#000000")
        val tag2 = Tag(id = 2, name = "Tag2", color = "#111111")
        val tag3 = Tag(id = 3, name = "Tag3", color = "#222222")

        viewModel.deleteTag(tag1)
        viewModel.deleteTag(tag2)
        viewModel.deleteTag(tag3)

        assertEquals(3, fakeTagDao.deletedTags.size)
    }

    /**
     * Test tags flow updates when dao emits new values
     */
    @Test
    fun tagsFlow_updatesWhenDaoEmits() = runTest {
        viewModel.loadTags()

        // Initially empty
        assertEquals(0, viewModel.tags.first().size)

        // Add a tag
        fakeTagDao.emitTags(listOf(Tag(id = 1, name = "New", color = "#FFFFFF")))

        // Now should have one tag
        assertEquals(1, viewModel.tags.first().size)
        assertEquals("New", viewModel.tags.first()[0].name)
    }
}
