package cat.company.qrreader.features.history.presentation

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class HistoryViewModelTest {

    // Minimal fake implementation of BarcodeRepository for unit tests
    private class FakeBarcodeRepository : BarcodeRepository {
        // Expose a mutable flow so tests can emit values
        private val resultFlow = MutableStateFlow<List<BarcodeWithTagsModel>>(emptyList())
        // Record the last parameters received
        var lastRequest: Triple<Int?, String?, Boolean?>? = null

        override fun getAllBarcodes(): Flow<List<BarcodeModel>> = flowOf(emptyList())

        override fun getBarcodesWithTags(): Flow<List<BarcodeWithTagsModel>> = resultFlow

        override fun getBarcodesWithTagsByFilter(
            tagId: Int?,
            query: String?,
            hideTaggedWhenNoTagSelected: Boolean
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

        fun emitResult(list: List<BarcodeWithTagsModel>) {
            resultFlow.value = list
        }
    }


    @Test
    fun onQueryChange_updatesState() {
        val repository = FakeBarcodeRepository()
        val getBarcodesUseCase = GetBarcodesWithTagsUseCase(repository)
        val updateBarcodeUseCase = UpdateBarcodeUseCase(repository)
        val deleteBarcodeUseCase = DeleteBarcodeUseCase(repository)
        val fakeSettingsRepo = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        }
        val vm = HistoryViewModel(getBarcodesUseCase, updateBarcodeUseCase, deleteBarcodeUseCase, fakeSettingsRepo)

        vm.onQueryChange("abc")
        assertEquals("abc", vm.searchQuery.value)
    }

    @Test
    fun onTagSelected_updatesState() {
        val repository = FakeBarcodeRepository()
        val getBarcodesUseCase = GetBarcodesWithTagsUseCase(repository)
        val updateBarcodeUseCase = UpdateBarcodeUseCase(repository)
        val deleteBarcodeUseCase = DeleteBarcodeUseCase(repository)
        val fakeSettingsRepo = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
        }
        val vm = HistoryViewModel(getBarcodesUseCase, updateBarcodeUseCase, deleteBarcodeUseCase, fakeSettingsRepo)

        vm.onTagSelected(42)
        assertEquals(42, vm.selectedTagId.value)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun savedBarcodes_respectsDebounce_and_emitsResults() = runTest {
        val fakeRepository = FakeBarcodeRepository()
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
        val vm = HistoryViewModel(getBarcodesUseCase, updateBarcodeUseCase, deleteBarcodeUseCase, fakeSettingsRepo)

        val collected = mutableListOf<List<BarcodeWithTagsModel>>()
        val job = launch { vm.savedBarcodes.collect { collected.add(it) } }

        // Change query and tag
        vm.onTagSelected(7)
        vm.onQueryChange("  abc  ") // will be trimmed to "abc"

        // Advance virtual time by debounce interval (250ms) so the ViewModel queries the repository
        advanceTimeBy(250)
        // Run pending tasks produced by the advance
        runCurrent()

        // Now repository should have been called with trimmed query
        // When query is non-blank, the use case sets tagId to null
        assertEquals(null, fakeRepository.lastRequest?.first)
        assertEquals("abc", fakeRepository.lastRequest?.second)

        // Emit a sample result and ensure it's received
        val sampleBarcode = BarcodeModel(id = 1, type = 1, format = 1, barcode = "X", date = Date())
        val sample = listOf(BarcodeWithTagsModel(sampleBarcode, emptyList()))
        fakeRepository.emitResult(sample)

        // Allow the dispatcher to run pending tasks
        advanceTimeBy(1)
        runCurrent()

        // There should be at least one emission (initial empty plus the new sample)
        // The last collected value should be the sample
        assertEquals(sample, collected.last())

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchIgnoresSelectedTag_whenSettingEnabled_andQueryNonEmpty() = runTest {
        val fakeRepository = FakeBarcodeRepository()

        // Settings: enable search across all tags
        val settingsHideFlag = MutableStateFlow(false)
        val settingsSearchAcrossAll = MutableStateFlow(true)

        val fakeSettingsRepository = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = settingsHideFlag

            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {
                settingsHideFlag.value = value
            }

            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = settingsSearchAcrossAll

            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {
                settingsSearchAcrossAll.value = value
            }
        }

        val getBarcodesUseCase = GetBarcodesWithTagsUseCase(fakeRepository)
        val updateBarcodeUseCase = UpdateBarcodeUseCase(fakeRepository)
        val deleteBarcodeUseCase = DeleteBarcodeUseCase(fakeRepository)

        val vm = HistoryViewModel(getBarcodesUseCase, updateBarcodeUseCase, deleteBarcodeUseCase, fakeSettingsRepository)

        val collected = mutableListOf<List<BarcodeWithTagsModel>>()
        val job = launch { vm.savedBarcodes.collect { collected.add(it) } }

        // Select a tag, then type a query
        vm.onTagSelected(77)
        vm.onQueryChange("searchme")

        // Advance by debounce interval
        advanceTimeBy(250)
        runCurrent()

        // The repository should have been called with tagId = null because searchAcrossAllTagsWhenFiltering=true
        assertEquals(null, fakeRepository.lastRequest?.first)
        assertEquals("searchme", fakeRepository.lastRequest?.second)

        job.cancel()
    }
}
