package cat.company.qrreader.history

import androidx.room.InvalidationTracker
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.daos.SavedBarcodeDao
import cat.company.qrreader.db.daos.TagDao
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
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

class HistoryViewModelTest {

    // Minimal fake implementation of SavedBarcodeDao for unit tests
    private class FakeSavedBarcodeDao : SavedBarcodeDao() {
        // Expose a mutable flow so tests can emit values
        private val resultFlow = MutableStateFlow<List<SavedBarcodeWithTags>>(emptyList())
        // Record the last parameters received
        var lastRequest: Pair<Int?, String?>? = null

        override fun getAll(): Flow<List<SavedBarcode>> = flowOf(emptyList())
        override fun getSavedBarcodesWithTags(): Flow<List<SavedBarcodeWithTags>> = resultFlow
        override fun getSavedBarcodesWithTagsByTagIdAndQuery(tagId: Int?, query: String?): Flow<List<SavedBarcodeWithTags>> {
            lastRequest = tagId to query
            return resultFlow
        }
        override suspend fun insertAll(vararg savedBarcodes: SavedBarcode) {}
        override suspend fun insertBarcodeTag(barcodeTag: cat.company.qrreader.db.entities.BarcodeTagCrossRef) {}
        override suspend fun removeBarcodeTag(barcodeTag: cat.company.qrreader.db.entities.BarcodeTagCrossRef) {}
        override suspend fun updateItem(savedBarcode: SavedBarcode): Int { return 0 }
        override suspend fun delete(barcode: SavedBarcode) {}

        fun emitResult(list: List<SavedBarcodeWithTags>) {
            resultFlow.value = list
        }
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

        // RoomDatabase abstract members - not used in unit tests, provide noop/unsupported implementations
        override fun createInvalidationTracker(): InvalidationTracker {
            throw UnsupportedOperationException("Not needed for unit tests")
        }

        override fun clearAllTables() {
            throw UnsupportedOperationException("Not needed for unit tests")
        }
    }

    @Test
    fun onQueryChange_updatesState() {
        val db = FakeBarcodesDb(FakeSavedBarcodeDao())
        val vm = HistoryViewModel(db)
        vm.onQueryChange("abc")
        assertEquals("abc", vm.searchQuery.value)
    }

    @Test
    fun onTagSelected_updatesState() {
        val db = FakeBarcodesDb(FakeSavedBarcodeDao())
        val vm = HistoryViewModel(db)
        vm.onTagSelected(42)
        assertEquals(42, vm.selectedTagId.value)
    }

    @Test
    fun factory_createsViewModel() {
        val db = FakeBarcodesDb(FakeSavedBarcodeDao())
        val factory = HistoryViewModelFactory(db)
        val vm = factory.create(HistoryViewModel::class.java)
        assertEquals(HistoryViewModel::class.java, vm::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun savedBarcodes_respectsDebounce_and_emitsResults() = runTest {
        val fakeDao = FakeSavedBarcodeDao()
        val db = FakeBarcodesDb(fakeDao)
        val vm = HistoryViewModel(db)

        val collected = mutableListOf<List<SavedBarcodeWithTags>>()
        val job = launch { vm.savedBarcodes.collect { collected.add(it) } }

        // Change query and tag
        vm.onTagSelected(7)
        vm.onQueryChange("  abc  ") // will be trimmed to "abc"

        // Advance virtual time by debounce interval (250ms) so the ViewModel queries the DAO
        advanceTimeBy(250)
        // Run pending tasks produced by the advance
        runCurrent()

        // Now DAO should have been called with trimmed query
        assertEquals(7, fakeDao.lastRequest?.first)
        assertEquals("abc", fakeDao.lastRequest?.second)

        // Emit a sample result and ensure it's received
        val sampleBarcode = SavedBarcode(id = 1, type = 1, format = 1, barcode = "X")
        val sample = listOf(SavedBarcodeWithTags(sampleBarcode, emptyList()))
        fakeDao.emitResult(sample)

        // Allow the dispatcher to run pending tasks
        advanceTimeBy(1)
        runCurrent()

        // There should be at least one emission (initial empty plus the new sample)
        // The last collected value should be the sample
        assertEquals(sample, collected.last())

        job.cancel()
    }
}
