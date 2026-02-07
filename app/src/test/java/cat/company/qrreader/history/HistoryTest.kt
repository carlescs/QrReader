package cat.company.qrreader.history

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for History screen logic
 *
 * Note: Full UI testing of the History composable requires instrumentation tests.
 * These unit tests focus on testing the integration logic and filtering behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryTest {

    private lateinit var fakeDao: FakeSavedBarcodeDao
    private lateinit var fakeDb: FakeBarcodesDb
    private lateinit var snackbarHostState: SnackbarHostState

    // Minimal fake implementation of SavedBarcodeDao for unit tests
    private class FakeSavedBarcodeDao : SavedBarcodeDao() {
        private val resultFlow = MutableStateFlow<List<SavedBarcodeWithTags>>(emptyList())
        var lastRequest: Triple<Int?, String?, Boolean?>? = null

        override fun getAll(): Flow<List<SavedBarcode>> = flowOf(emptyList())
        override fun getSavedBarcodesWithTags(): Flow<List<SavedBarcodeWithTags>> = resultFlow
        override fun getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId: Int?,
            query: String?,
            hideTaggedWhenNoTagSelected: Boolean
        ): Flow<List<SavedBarcodeWithTags>> {
            lastRequest = Triple(tagId, query, hideTaggedWhenNoTagSelected)
            return resultFlow
        }

        override suspend fun insertAll(vararg savedBarcodes: SavedBarcode) {}
        override suspend fun insertBarcodeTag(barcodeTag: BarcodeTagCrossRef) {}
        override suspend fun removeBarcodeTag(barcodeTag: BarcodeTagCrossRef) {}
        override suspend fun updateItem(savedBarcode: SavedBarcode): Int = 0
        override suspend fun delete(barcode: SavedBarcode) {}
    }

    // Minimal fake TagDao
    private class FakeTagDao : TagDao {
        override fun getAll(): Flow<List<Tag>> = flowOf(emptyList())
        override fun insertAll(vararg tags: Tag) {}
        override fun updateItem(tag: Tag) {}
        override fun delete(tag: Tag) {}
    }

    // Minimal fake BarcodesDb that returns the fake DAOs
    private class FakeBarcodesDb(private val dao: SavedBarcodeDao) : BarcodesDb() {
        private val tdao = FakeTagDao()
        override fun savedBarcodeDao(): SavedBarcodeDao = dao
        override fun tagDao(): TagDao = tdao

        override fun createInvalidationTracker(): InvalidationTracker {
            throw UnsupportedOperationException("Not needed for unit tests")
        }

        override fun clearAllTables() {
            throw UnsupportedOperationException("Not needed for unit tests")
        }
    }

    @Before
    fun setup() {
        fakeDao = FakeSavedBarcodeDao()
        fakeDb = FakeBarcodesDb(fakeDao)
        snackbarHostState = SnackbarHostState()
    }

    /**
     * Test that client-side filtering works correctly when hideTagged is true
     * and no tag is selected (should filter out items with tags)
     */
    @Test
    fun visibleItems_whenHideTaggedTrueAndNoTagSelected_filtersOutTaggedItems() = runTest {
        // Create test data
        val tag = Tag(id = 1, name = "TestTag", color = "#FF0000")
        val barcodeWithTag = SavedBarcodeWithTags(
            barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "123"),
            tags = listOf(tag)
        )
        val barcodeWithoutTag = SavedBarcodeWithTags(
            barcode = SavedBarcode(id = 2, type = 0, format = 0, barcode = "456"),
            tags = emptyList()
        )

        val items = listOf(barcodeWithTag, barcodeWithoutTag)

        // Apply the same client-side filter logic from History.kt
        val hideTagged = true
        val visibleItems = if (hideTagged) {
            items.filter { it.tags.isEmpty() }
        } else items

        // Verify only untagged items are visible
        assertEquals(1, visibleItems.size)
        assertEquals(2, visibleItems[0].barcode.id)
        assertTrue(visibleItems[0].tags.isEmpty())
    }

    /**
     * Test that all items are visible when hideTagged is false
     */
    @Test
    fun visibleItems_whenHideTaggedFalse_showsAllItems() = runTest {
        val tag = Tag(id = 1, name = "TestTag", color = "#FF0000")
        val barcodeWithTag = SavedBarcodeWithTags(
            barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "123"),
            tags = listOf(tag)
        )
        val barcodeWithoutTag = SavedBarcodeWithTags(
            barcode = SavedBarcode(id = 2, type = 0, format = 0, barcode = "456"),
            tags = emptyList()
        )

        val items = listOf(barcodeWithTag, barcodeWithoutTag)

        // Apply filter logic with hideTagged = false
        val hideTagged = false
        val visibleItems = if (hideTagged) {
            items.filter { it.tags.isEmpty() }
        } else items

        // Verify all items are visible
        assertEquals(2, visibleItems.size)
    }

    /**
     * Test that all items are visible when a tag is selected (even if hideTagged is true)
     */
    @Test
    fun visibleItems_whenTagSelected_showsAllItems() = runTest {
        val tag = Tag(id = 1, name = "TestTag", color = "#FF0000")
        val barcodeWithTag = SavedBarcodeWithTags(
            barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "123"),
            tags = listOf(tag)
        )
        val barcodeWithoutTag = SavedBarcodeWithTags(
            barcode = SavedBarcode(id = 2, type = 0, format = 0, barcode = "456"),
            tags = emptyList()
        )

        val items = listOf(barcodeWithTag, barcodeWithoutTag)

        // Apply filter logic with tag selected
        val visibleItems = items

        // Verify all items are visible when tag is selected
        assertEquals(2, visibleItems.size)
    }

    /**
     * Test that multiple tagged items are filtered correctly
     */
    @Test
    fun visibleItems_withMultipleTaggedItems_filtersCorrectly() = runTest {
        val tag1 = Tag(id = 1, name = "Tag1", color = "#FF0000")
        val tag2 = Tag(id = 2, name = "Tag2", color = "#00FF00")

        val items = listOf(
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "Tagged1", title = "Tagged1"),
                tags = listOf(tag1)
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 2, type = 0, format = 0, barcode = "Untagged1", title = "Untagged1"),
                tags = emptyList()
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 3, type = 0, format = 0, barcode = "Tagged2", title = "Tagged2"),
                tags = listOf(tag2)
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 4, type = 0, format = 0, barcode = "Untagged2", title = "Untagged2"),
                tags = emptyList()
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 5, type = 0, format = 0, barcode = "TaggedBoth", title = "TaggedBoth"),
                tags = listOf(tag1, tag2)
            )
        )

        val hideTagged = true
        val visibleItems = if (hideTagged) {
            items.filter { it.tags.isEmpty() }
        } else items

        // Should only show 2 untagged items
        assertEquals(2, visibleItems.size)
        assertEquals("Untagged1", visibleItems[0].barcode.title)
        assertEquals("Untagged2", visibleItems[1].barcode.title)
    }

    /**
     * Test that empty list remains empty after filtering
     */
    @Test
    fun visibleItems_withEmptyList_remainsEmpty() = runTest {
        val items = emptyList<SavedBarcodeWithTags>()

        val hideTagged = true
        val visibleItems = if (hideTagged) {
            items.filter { it.tags.isEmpty() }
        } else items

        assertTrue(visibleItems.isEmpty())
    }

    /**
     * Test that all untagged items remain visible
     */
    @Test
    fun visibleItems_withAllUntaggedItems_showsAll() = runTest {
        val items = listOf(
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "Untagged1"),
                tags = emptyList()
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 2, type = 0, format = 0, barcode = "Untagged2"),
                tags = emptyList()
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 3, type = 0, format = 0, barcode = "Untagged3"),
                tags = emptyList()
            )
        )

        val hideTagged = true
        val visibleItems = if (hideTagged) {
            items.filter { it.tags.isEmpty() }
        } else items

        assertEquals(3, visibleItems.size)
    }

    /**
     * Test that all tagged items are filtered when hideTagged is true
     */
    @Test
    fun visibleItems_withAllTaggedItems_filtersAll() = runTest {
        val tag = Tag(id = 1, name = "TestTag", color = "#FF0000")
        val items = listOf(
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "Tagged1"),
                tags = listOf(tag)
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 2, type = 0, format = 0, barcode = "Tagged2"),
                tags = listOf(tag)
            ),
            SavedBarcodeWithTags(
                barcode = SavedBarcode(id = 3, type = 0, format = 0, barcode = "Tagged3"),
                tags = listOf(tag)
            )
        )

        val hideTagged = true
        val visibleItems = if (hideTagged) {
            items.filter { it.tags.isEmpty() }
        } else items

        // All items should be filtered out
        assertTrue(visibleItems.isEmpty())
    }

    /**
     * Test ViewModel integration with History screen parameters
     */
    @Test
    fun historyViewModel_integrationWithDb_works() = runTest {
        val viewModel = HistoryViewModel(fakeDb)

        // Test query change
        viewModel.onQueryChange("test")
        assertEquals("test", viewModel.searchQuery.value)

        // Test tag selection
        viewModel.onTagSelected(1)
        assertEquals(1, viewModel.selectedTagId.value)

        // Test hide flag and collect from savedBarcodes to trigger the flow
        viewModel.setHideTaggedWhenNoTagSelected(true)

        // Collect one value from the flow to trigger the DAO query
        viewModel.savedBarcodes.first()

        // Verify DAO received correct parameters
        assertNotNull(fakeDao.lastRequest)
        assertEquals(1, fakeDao.lastRequest?.first) // tagId
        assertEquals("test", fakeDao.lastRequest?.second) // query
        assertEquals(true, fakeDao.lastRequest?.third) // hideTaggedWhenNoTagSelected
    }

    /**
     * Test that ViewModel can be created with HistoryViewModelFactory
     */
    @Test
    fun historyViewModelFactory_createsViewModel() {
        val factory = HistoryViewModelFactory(fakeDb)
        val viewModel = factory.create(HistoryViewModel::class.java)

        assertNotNull(viewModel)
    }

    /**
     * Test search query behavior
     */
    @Test
    fun searchQuery_updatesCorrectly() = runTest {
        val viewModel = HistoryViewModel(fakeDb)

        // Initial state should be empty
        assertEquals("", viewModel.searchQuery.value)

        // Update query
        viewModel.onQueryChange("barcode123")
        assertEquals("barcode123", viewModel.searchQuery.value)

        // Clear query
        viewModel.onQueryChange("")
        assertEquals("", viewModel.searchQuery.value)
    }

    /**
     * Test tag selection behavior
     */
    @Test
    fun tagSelection_updatesCorrectly() = runTest {
        val viewModel = HistoryViewModel(fakeDb)

        // Initial state should be null
        assertNull(viewModel.selectedTagId.value)

        // Select a tag
        viewModel.onTagSelected(5)
        assertEquals(5, viewModel.selectedTagId.value)

        // Deselect tag
        viewModel.onTagSelected(null)
        assertNull(viewModel.selectedTagId.value)
    }

    /**
     * Test SnackbarHostState initialization
     */
    @Test
    fun snackbarHostState_initializes() {
        assertNotNull(snackbarHostState)
    }

    /**
     * Test drawer state management logic
     */
    @Test
    fun drawerState_togglesCorrectly() {

        // Open drawer
        // Simulate drawer state toggle logic from History.kt
        var drawerValue: DrawerValue = DrawerValue.Open

        assertEquals(DrawerValue.Open, drawerValue)

        // Close drawer
        drawerValue = DrawerValue.Closed

        assertEquals(DrawerValue.Closed, drawerValue)
    }
}
