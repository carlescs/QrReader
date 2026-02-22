package cat.company.qrreader.features.history.presentation

import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.domain.repository.BarcodeRepository
import cat.company.qrreader.domain.usecase.barcode.GenerateBarcodeAiDataUseCase
import cat.company.qrreader.domain.usecase.history.DeleteBarcodeUseCase
import cat.company.qrreader.domain.usecase.history.GetBarcodesWithTagsUseCase
import cat.company.qrreader.domain.usecase.history.UpdateBarcodeUseCase
import cat.company.qrreader.domain.usecase.settings.GetAiLanguageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryViewModelHideFlagTest {

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

        override suspend fun insertBarcodeAndGetId(barcode: BarcodeModel): Long = 0L

        override suspend fun updateBarcode(barcode: BarcodeModel): Int = 0

        override suspend fun deleteBarcode(barcode: BarcodeModel) {}

        override suspend fun addTagToBarcode(barcodeId: Int, tagId: Int) {}

        override suspend fun removeTagFromBarcode(barcodeId: Int, tagId: Int) {}

        override suspend fun switchTag(barcode: BarcodeWithTagsModel, tag: TagModel) {}
    }

    private class FakeGenerateBarcodeAiDataUseCase : GenerateBarcodeAiDataUseCase() {
        override suspend fun invoke(
            barcodeContent: String,
            barcodeType: String?,
            barcodeFormat: String?,
            existingTags: List<String>,
            language: String
        ) = Result.failure<cat.company.qrreader.domain.model.BarcodeAiData>(
            UnsupportedOperationException("AI not available in tests")
        )
        override suspend fun isAiSupportedOnDevice(): Boolean = true
        override suspend fun downloadModelIfNeeded() {}
        override fun cleanup() {}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun savedBarcodes_includes_hide_flag_in_dao_query() = runTest {
        val fakeRepository = FakeBarcodeRepository()
        val getBarcodesWithTagsUseCase = GetBarcodesWithTagsUseCase(fakeRepository)
        val updateBarcodeUseCase = UpdateBarcodeUseCase(fakeRepository)
        val deleteBarcodeUseCase = DeleteBarcodeUseCase(fakeRepository)

        val hideTaggedFlow = MutableStateFlow(false)
        val fakeSettingsRepo = object : cat.company.qrreader.domain.repository.SettingsRepository {
            override val hideTaggedWhenNoTagSelected: Flow<Boolean>
                get() = hideTaggedFlow
            override suspend fun setHideTaggedWhenNoTagSelected(value: Boolean) {}
            override val searchAcrossAllTagsWhenFiltering: Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun setSearchAcrossAllTagsWhenFiltering(value: Boolean) {}
            override val aiGenerationEnabled: kotlinx.coroutines.flow.Flow<Boolean>
                get() = kotlinx.coroutines.flow.flowOf(true)
            override suspend fun setAiGenerationEnabled(value: Boolean) {}
            override val aiLanguage: kotlinx.coroutines.flow.Flow<String>
                get() = kotlinx.coroutines.flow.flowOf("en")
            override suspend fun setAiLanguage(value: String) {}
        }
        val vm = HistoryViewModel(
            getBarcodesWithTagsUseCase,
            updateBarcodeUseCase,
            deleteBarcodeUseCase,
            fakeSettingsRepo,
            FakeGenerateBarcodeAiDataUseCase(),
            GetAiLanguageUseCase(fakeSettingsRepo)
        )

        // Collect savedBarcodes to trigger the Flow
        val collected = mutableListOf<List<BarcodeWithTagsModel>>()
        val job = launch { vm.savedBarcodes.collect { collected.add(it) } }

        // Initially hide flag false
        vm.onTagSelected(null)
        vm.onQueryChange("")
        advanceTimeBy(250)
        runCurrent()
        assertEquals(false, fakeRepository.lastRequest?.third ?: false)

        // Now set hide flag true in settings and ensure repository gets called with true
        hideTaggedFlow.value = true
        advanceTimeBy(50)
        runCurrent()
        // trigger the debounce time to ensure query fires
        advanceTimeBy(250)
        runCurrent()

        assertEquals(true, fakeRepository.lastRequest?.third)

        job.cancel()
    }
}
