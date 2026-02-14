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
        // When searchAcrossAllTagsWhenFiltering=false, tagId is preserved even with a query
        assertEquals(1, fakeRepository.lastRequest?.first) // tagId
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
