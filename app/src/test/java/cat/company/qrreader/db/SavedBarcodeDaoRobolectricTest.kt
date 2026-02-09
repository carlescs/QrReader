package cat.company.qrreader.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cat.company.qrreader.db.daos.SavedBarcodeDao
import cat.company.qrreader.db.entities.BarcodeTagCrossRef
import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SavedBarcodeDaoRobolectricTest {

    private lateinit var db: BarcodesDb
    private lateinit var dao: SavedBarcodeDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, BarcodesDb::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.savedBarcodeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `non blank query should return tagged items even when hideTaggedWhenNoTagSelected is true and no tag selected`() = runTest {
        // Insert a tagged barcode
        val tagged = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Tagged", barcode = "ABC123")
        val tag = Tag(id = 1, name = "T", color = "#fff")
        // Call DAO suspend methods
        db.tagDao().insertAll(tag)
        dao.insertAll(tagged)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = tagged.id, tagId = tag.id))

        // Query with non-blank term; no tag selected (tagId = null); hide flag true
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(tagId = null, query = "ABC", hideTaggedWhenNoTagSelected = true).first()

        // We expect the tagged barcode to be returned because query is non-blank
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(tagged.id, result.barcode.id)
        // ensure tag is present
        assertEquals(1, result.tags.size)
        assertEquals(tag.id, result.tags[0].id)
    }
}
