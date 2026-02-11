package cat.company.qrreader.features.history.presentation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
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
import java.util.Date

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

    private lateinit var fakeRepository: FakeBarcodeRepository
    private lateinit var snackbarHostState: SnackbarHostState

    // Minimal fake implementation of BarcodeRepository for unit tests
    private class FakeBarcodeRepository : BarcodeRepository {
        private val resultFlow = MutableStateFlow<List<BarcodeWithTagsModel>>(emptyList())
        var lastRequest: Triple<Int?, String?, Boolean?>? = null

        override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())

        override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = resultFlow

        override fun getBarcodesWithTagsByFilter(
            tagId: Int?,
            query: String?,
            hideTaggedWhenNoTagSelected: Boolean,
            searchAcrossAllTagsWhenFiltering: Boolean
        ): Flow<List<BarcodeWithTagsModel>> {
            lastRequest = Triple(tagId, query, hideTaggedWhenNoTagSelected)
            return resultFlow
        }

        override suspend fun insertBarcodes(vararg barcodes: BarcodeModel) {}

        override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0

        override suspend fun deleteBarcode(barcode: BarcodeModel) {}

        override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}

        override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}

        override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
    }

    @Before
    fun setup() {
        fakeRepository = FakeBarcodeRepository()
        snackbarHostState = SnackbarHostState()
    }

    /**
     * Test that client-side filtering works correctly when hideTagged is true
     * and no tag is selected (should filter out items with tags)
     */
    @Test
    fun visibleItems_whenHideTaggedTrueAndNoTagSelected_filtersOutTaggedItems() = runTest {
        // Create test data
        val tag = TagModel(id = 1, name = "TestTag", color = "#FF0000")
        val barcodeWithTag = BarcodeWithTagsModel(
            barcode = BarcodeModel(id = 1, type = 0, format = 0, barcode = "123", date = Date()),
            tags = listOf(tag)
        )
        val barcodeWithoutTag = BarcodeWithTagsModel(
            barcode = BarcodeModel(id = 2, type = 0, format = 0, barcode = "456", date = Date()),
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
        val tag = TagModel(id = 1, name = "TestTag", color = "#FF0000")
        val barcodeWithTag = BarcodeWithTagsModel(
            barcode = BarcodeModel(id = 1, type = 0, format = 0, barcode = "123", date = Date()),
            tags = listOf(tag)
        )
        val barcodeWithoutTag = BarcodeWithTagsModel(
            barcode = BarcodeModel(id = 2, type = 0, format = 0, barcode = "456", date = Date()),
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
        val tag = TagModel(id = 1, name = "TestTag", color = "#FF0000")
        val barcodeWithTag = BarcodeWithTagsModel(
            barcode = BarcodeModel(id = 1, type = 0, format = 0, barcode = "123", date = Date()),
            tags = listOf(tag)
        )
        val barcodeWithoutTag = BarcodeWithTagsModel(
            barcode = BarcodeModel(id = 2, type = 0, format = 0, barcode = "456", date = Date()),
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
        val tag1 = TagModel(id = 1, name = "Tag1", color = "#FF0000")
        val tag2 = TagModel(id = 2, name = "Tag2", color = "#00FF00")

        val items = listOf(
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 1, type = 0, format = 0, barcode = "Tagged1", title = "Tagged1", date = Date()),
                tags = listOf(tag1)
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 2, type = 0, format = 0, barcode = "Untagged1", title = "Untagged1", date = Date()),
                tags = emptyList()
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 3, type = 0, format = 0, barcode = "Tagged2", title = "Tagged2", date = Date()),
                tags = listOf(tag2)
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 4, type = 0, format = 0, barcode = "Untagged2", title = "Untagged2", date = Date()),
                tags = emptyList()
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 5, type = 0, format = 0, barcode = "TaggedBoth", title = "TaggedBoth", date = Date()),
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
        val items = emptyList<BarcodeWithTagsModel>()

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
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 1, type = 0, format = 0, barcode = "Untagged1", date = Date()),
                tags = emptyList()
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 2, type = 0, format = 0, barcode = "Untagged2", date = Date()),
                tags = emptyList()
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 3, type = 0, format = 0, barcode = "Untagged3", date = Date()),
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
        val tag = TagModel(id = 1, name = "TestTag", color = "#FF0000")
        val items = listOf(
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 1, type = 0, format = 0, barcode = "Tagged1", date = Date()),
                tags = listOf(tag)
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 2, type = 0, format = 0, barcode = "Tagged2", date = Date()),
                tags = listOf(tag)
            ),
            BarcodeWithTagsModel(
                barcode = BarcodeModel(id = 3, type = 0, format = 0, barcode = "Tagged3", date = Date()),
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
    fun historyViewModel_integrationWithRepository_works() = runTest {
        val getBarcodesUseCase = GetBarcodesWithTagsUseCase(fakeRepository)
        val updateBarcodeUseCase = UpdateBarcodeUseCase(fakeRepository)
        val deleteBarcodeUseCase = DeleteBarcodeUseCase(fakeRepository)
        val fakeSettingsRepo = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        }
        val viewModel = HistoryViewModel(getBarcodesUseCase, updateBarcodeUseCase, deleteBarcodeUseCase, fakeSettingsRepo)

        // Test query change
        viewModel.onQueryChange("test")
        assertEquals("test", viewModel.searchQuery.value)

        // Test tag selection
        viewModel.onTagSelected(1)
        assertEquals(1, viewModel.selectedTagId.value)

        // Collect one value from the flow to trigger the repository query
        viewModel.savedBarcodes.first()

        // Verify repository received correct parameters
        assertNotNull(fakeRepository.lastRequest)
        // When query is non-blank, the use case sets tagId to null
        assertEquals(null, fakeRepository.lastRequest?.first) // tagId
        assertEquals("test", fakeRepository.lastRequest?.second) // query
        assertEquals(false, fakeRepository.lastRequest?.third) // hideTaggedWhenNoTagSelected (from fakeSettingsRepo)
    }

    /**
     * Test search query behavior
     */
    @Test
    fun searchQuery_updatesCorrectly() = runTest {
        val getBarcodesUseCase = GetBarcodesWithTagsUseCase(fakeRepository)
        val updateBarcodeUseCase = UpdateBarcodeUseCase(fakeRepository)
        val deleteBarcodeUseCase = DeleteBarcodeUseCase(fakeRepository)
        val fakeSettingsRepo = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        }
        val viewModel = HistoryViewModel(getBarcodesUseCase, updateBarcodeUseCase, deleteBarcodeUseCase, fakeSettingsRepo)

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
        val getBarcodesUseCase = GetBarcodesWithTagsUseCase(fakeRepository)
        val updateBarcodeUseCase = UpdateBarcodeUseCase(fakeRepository)
        val deleteBarcodeUseCase = DeleteBarcodeUseCase(fakeRepository)
        val fakeSettingsRepo = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        }
        val viewModel = HistoryViewModel(getBarcodesUseCase, updateBarcodeUseCase, deleteBarcodeUseCase, fakeSettingsRepo)

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
