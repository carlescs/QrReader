package cat.company.qrreader.history

import androidx.room.InvalidationTracker
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.daos.SavedBarcodeDao
import cat.company.qrreader.db.daos.TagDao
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
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

    private class FakeSavedBarcodeDao : SavedBarcodeDao() {
        private val resultFlow = MutableStateFlow<List<SavedBarcodeWithTags>>(emptyList())
        var lastRequest: Triple<Int?, String?, Boolean?>? = null

        override fun getAll(): Flow<List<SavedBarcode>> = flowOf(emptyList())
        override fun getSavedBarcodesWithTags(): Flow<List<SavedBarcodeWithTags>> = resultFlow
        override fun getSavedBarcodesWithTagsByTagIdAndQuery(tagId: Int?, query: String?, hideTaggedWhenNoTagSelected: Boolean): Flow<List<SavedBarcodeWithTags>> {
            lastRequest = Triple(tagId, query, hideTaggedWhenNoTagSelected)
            return resultFlow
        }

        override suspend fun insertAll(vararg savedBarcodes: SavedBarcode) {}
        override suspend fun insertBarcodeTag(barcodeTag: cat.company.qrreader.db.entities.BarcodeTagCrossRef) {}
        override suspend fun removeBarcodeTag(barcodeTag: cat.company.qrreader.db.entities.BarcodeTagCrossRef) {}
        override suspend fun updateItem(savedBarcode: SavedBarcode): Int { return 0 }
        override suspend fun delete(barcode: SavedBarcode) {}
    }

    private class FakeTagDao : TagDao {
        override fun getAll(): Flow<List<Tag>> = flowOf(emptyList())
        override fun insertAll(vararg tags: Tag) {}
        override fun updateItem(tag: Tag) {}
        override fun delete(tag: Tag) {}
    }

    private class FakeBarcodesDb(private val dao: SavedBarcodeDao) : BarcodesDb() {
        private val tdao = FakeTagDao()
        override fun savedBarcodeDao(): SavedBarcodeDao = dao
        override fun tagDao(): TagDao = tdao
        override fun createInvalidationTracker(): InvalidationTracker { throw UnsupportedOperationException() }
        override fun clearAllTables() { throw UnsupportedOperationException() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun savedBarcodes_includes_hide_flag_in_dao_query() = runTest {
        val fakeDao = FakeSavedBarcodeDao()
        val db = FakeBarcodesDb(fakeDao)
        val vm = HistoryViewModel(db)

        // Collect savedBarcodes to trigger the Flow
        val collected = mutableListOf<List<SavedBarcodeWithTags>>()
        val job = launch { vm.savedBarcodes.collect { collected.add(it) } }

        // Initially hide flag false
        vm.onTagSelected(null)
        vm.onQueryChange("")
        advanceTimeBy(250)
        runCurrent()
        assertEquals(false, fakeDao.lastRequest?.third ?: false)

        // Now set hide flag true and ensure DAO gets called with true
        vm.setHideTaggedWhenNoTagSelected(true)
        advanceTimeBy(50)
        runCurrent()
        // trigger the debounce time to ensure query fires
        advanceTimeBy(250)
        runCurrent()

        assertEquals(true, fakeDao.lastRequest?.third)

        job.cancel()
    }
}
