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
    fun `hideTaggedWhenNoTagSelected with query but searchAcrossAllTagsWhenFiltering false should hide tagged items`() = runTest {
        // Insert tagged and untagged barcodes
        val tagged = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Tagged", barcode = "ABC123")
        val untagged = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Untagged", barcode = "ABC456")
        val tag = Tag(id = 1, name = "T", color = "#fff")

        db.tagDao().insertAll(tag)
        dao.insertAll(tagged, untagged)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = tagged.id, tagId = tag.id))

        // Query with non-blank term; no tag selected (tagId = null); hide flag true; searchAcrossAllTagsWhenFiltering = false
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = null,
            query = "ABC",
            hideTaggedWhenNoTagSelected = true,
            searchAcrossAllTagsWhenFiltering = false,
            showOnlyFavorites = false
        ).first()

        // Should hide tagged items because searchAcrossAllTagsWhenFiltering is false
        // Only the untagged barcode should be returned
        assertEquals(1, results.size)
        assertEquals(untagged.id, results[0].barcode.id)
        assertEquals(0, results[0].tags.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `hideTaggedWhenNoTagSelected with query and searchAcrossAllTagsWhenFiltering true should show tagged items`() = runTest {
        // Insert tagged and untagged barcodes
        val tagged = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Tagged", barcode = "ABC123")
        val untagged = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Untagged", barcode = "ABC456")
        val tag = Tag(id = 1, name = "T", color = "#fff")

        db.tagDao().insertAll(tag)
        dao.insertAll(tagged, untagged)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = tagged.id, tagId = tag.id))

        // Query with non-blank term; no tag selected (tagId = null); hide flag true; searchAcrossAllTagsWhenFiltering = true
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = null,
            query = "ABC",
            hideTaggedWhenNoTagSelected = true,
            searchAcrossAllTagsWhenFiltering = true,
            showOnlyFavorites = false
        ).first()

        // Should show ALL matching items (both tagged and untagged) because searchAcrossAllTagsWhenFiltering is true
        assertEquals(2, results.size)
        val ids = results.map { it.barcode.id }.sorted()
        assertEquals(listOf(1, 2), ids)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `hideTaggedWhenNoTagSelected true with no query and no tag should hide tagged items`() = runTest {
        // Insert both tagged and untagged barcodes
        val tagged = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Tagged Item", barcode = "TAG123")
        val untagged = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Untagged Item", barcode = "NOTAG456")
        val tag = Tag(id = 1, name = "Important", color = "#ff0000")

        db.tagDao().insertAll(tag)
        dao.insertAll(tagged, untagged)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = tagged.id, tagId = tag.id))

        // Query with: no tag selected, no search query, hide flag true
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = null,
            query = null,
            hideTaggedWhenNoTagSelected = true,
            searchAcrossAllTagsWhenFiltering = false,
            showOnlyFavorites = false
        ).first()

        // Should only return the untagged barcode
        assertEquals(1, results.size)
        assertEquals(untagged.id, results[0].barcode.id)
        assertEquals(0, results[0].tags.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `hideTaggedWhenNoTagSelected true with empty query and no tag should hide tagged items`() = runTest {
        // Insert both tagged and untagged barcodes
        val tagged = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Tagged Item", barcode = "TAG123")
        val untagged = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Untagged Item", barcode = "NOTAG456")
        val tag = Tag(id = 1, name = "Important", color = "#ff0000")

        db.tagDao().insertAll(tag)
        dao.insertAll(tagged, untagged)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = tagged.id, tagId = tag.id))

        // Query with: no tag selected, empty/whitespace query, hide flag true
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = null,
            query = "  ",
            hideTaggedWhenNoTagSelected = true,
            searchAcrossAllTagsWhenFiltering = false,
            showOnlyFavorites = false
        ).first()

        // Should only return the untagged barcode (empty/whitespace query = no search)
        assertEquals(1, results.size)
        assertEquals(untagged.id, results[0].barcode.id)
        assertEquals(0, results[0].tags.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `hideTaggedWhenNoTagSelected false should return all items including tagged`() = runTest {
        // Insert both tagged and untagged barcodes
        val tagged = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Tagged Item", barcode = "TAG123")
        val untagged = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Untagged Item", barcode = "NOTAG456")
        val tag = Tag(id = 1, name = "Important", color = "#ff0000")

        db.tagDao().insertAll(tag)
        dao.insertAll(tagged, untagged)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = tagged.id, tagId = tag.id))

        // Query with: no tag selected, no query, hide flag FALSE
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = null,
            query = null,
            hideTaggedWhenNoTagSelected = false,
            searchAcrossAllTagsWhenFiltering = false,
            showOnlyFavorites = false
        ).first()

        // Should return both barcodes
        assertEquals(2, results.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `search query should find tagged items across title description and barcode fields`() = runTest {
        // Insert tagged barcodes with search terms in different fields
        val taggedInTitle = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "SearchTerm Here", description = "Other", barcode = "123")
        val taggedInDesc = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Other", description = "SearchTerm Here", barcode = "456")
        val taggedInBarcode = SavedBarcode(id = 3, date = Date(), type = 1, format = 1, title = "Other", description = "Other", barcode = "SearchTerm789")
        val notMatching = SavedBarcode(id = 4, date = Date(), type = 1, format = 1, title = "Nothing", description = "Nothing", barcode = "999")
        val tag = Tag(id = 1, name = "Tag", color = "#ff0000")

        db.tagDao().insertAll(tag)
        dao.insertAll(taggedInTitle, taggedInDesc, taggedInBarcode, notMatching)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = taggedInTitle.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = taggedInDesc.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = taggedInBarcode.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = notMatching.id, tagId = tag.id))

        // Search for "SearchTerm" with hideTaggedWhenNoTagSelected = true and searchAcrossAllTagsWhenFiltering = true
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = null,
            query = "SearchTerm",
            hideTaggedWhenNoTagSelected = true,
            searchAcrossAllTagsWhenFiltering = true,
            showOnlyFavorites = false
        ).first()

        // Should return only the 3 matching barcodes, not the non-matching one
        assertEquals(3, results.size)
        val ids = results.map { it.barcode.id }.sorted()
        assertEquals(listOf(1, 2, 3), ids)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `search query with tag selected should filter by both tag and query`() = runTest {
        // Insert barcodes with different tags
        val tag1 = Tag(id = 1, name = "Tag1", color = "#ff0000")
        val tag2 = Tag(id = 2, name = "Tag2", color = "#00ff00")
        val barcode1Tag1 = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Find Me", barcode = "123")
        val barcode2Tag1 = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Other", barcode = "456")
        val barcode3Tag2 = SavedBarcode(id = 3, date = Date(), type = 1, format = 1, title = "Find Me Too", barcode = "789")

        db.tagDao().insertAll(tag1, tag2)
        dao.insertAll(barcode1Tag1, barcode2Tag1, barcode3Tag2)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = barcode1Tag1.id, tagId = tag1.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = barcode2Tag1.id, tagId = tag1.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = barcode3Tag2.id, tagId = tag2.id))

        // Search for "Find" with tag1 selected, searchAcrossAllTagsWhenFiltering = false
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = 1,
            query = "Find",
            hideTaggedWhenNoTagSelected = true,
            searchAcrossAllTagsWhenFiltering = false,
            showOnlyFavorites = false
        ).first()

        // Should return only barcode1Tag1 (has tag1 AND matches "Find")
        assertEquals(1, results.size)
        assertEquals(barcode1Tag1.id, results[0].barcode.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `searchAcrossAllTagsWhenFiltering true should ignore tag filter when searching`() = runTest {
        // Insert barcodes with different tags
        val tag1 = Tag(id = 1, name = "Tag1", color = "#ff0000")
        val tag2 = Tag(id = 2, name = "Tag2", color = "#00ff00")
        val barcode1Tag1 = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Find Me", barcode = "123")
        val barcode2Tag1 = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Other", barcode = "456")
        val barcode3Tag2 = SavedBarcode(id = 3, date = Date(), type = 1, format = 1, title = "Find Me Too", barcode = "789")

        db.tagDao().insertAll(tag1, tag2)
        dao.insertAll(barcode1Tag1, barcode2Tag1, barcode3Tag2)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = barcode1Tag1.id, tagId = tag1.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = barcode2Tag1.id, tagId = tag1.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = barcode3Tag2.id, tagId = tag2.id))

        // Search for "Find" with tag1 selected BUT searchAcrossAllTagsWhenFiltering = true
        val results = dao.getSavedBarcodesWithTagsByTagIdAndQuery(
            tagId = 1,
            query = "Find",
            hideTaggedWhenNoTagSelected = false,
            searchAcrossAllTagsWhenFiltering = true,
            showOnlyFavorites = false
        ).first()

        // Should return BOTH barcodes that match "Find", ignoring the tag filter
        assertEquals(2, results.size)
        val ids = results.map { it.barcode.id }.sorted()
        assertEquals(listOf(1, 3), ids)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getTagBarcodeCountsFiltered with no filters should return all counts`() = runTest {
        val tag = Tag(id = 1, name = "Tag1", color = "#ff0000")
        val b1 = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "B1", barcode = "111")
        val b2 = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "B2", barcode = "222")

        db.tagDao().insertAll(tag)
        dao.insertAll(b1, b2)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = b1.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = b2.id, tagId = tag.id))

        val counts = dao.getTagBarcodeCountsFiltered(
            showOnlyFavorites = false,
            showOnlyLocked = false,
            hideLocked = false,
            query = null
        ).first()

        assertEquals(2, counts.first { it.tagId == tag.id }.count)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getTagBarcodeCountsFiltered with showOnlyFavorites should count only favorites`() = runTest {
        val tag = Tag(id = 1, name = "Tag1", color = "#ff0000")
        val fav = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Fav", barcode = "111", isFavorite = true)
        val notFav = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "NotFav", barcode = "222")

        db.tagDao().insertAll(tag)
        dao.insertAll(fav, notFav)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = fav.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = notFav.id, tagId = tag.id))

        val counts = dao.getTagBarcodeCountsFiltered(
            showOnlyFavorites = true,
            showOnlyLocked = false,
            hideLocked = false,
            query = null
        ).first()

        assertEquals(1, counts.first { it.tagId == tag.id }.count)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getTagBarcodeCountsFiltered with hideLocked should exclude locked barcodes`() = runTest {
        val tag = Tag(id = 1, name = "Tag1", color = "#ff0000")
        val normal = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Normal", barcode = "111")
        val locked = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Locked", barcode = "222", isLocked = true)

        db.tagDao().insertAll(tag)
        dao.insertAll(normal, locked)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = normal.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = locked.id, tagId = tag.id))

        val counts = dao.getTagBarcodeCountsFiltered(
            showOnlyFavorites = false,
            showOnlyLocked = false,
            hideLocked = true,
            query = null
        ).first()

        assertEquals(1, counts.first { it.tagId == tag.id }.count)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getTagBarcodeCountsFiltered with showOnlyLocked should count only locked barcodes`() = runTest {
        val tag = Tag(id = 1, name = "Tag1", color = "#ff0000")
        val normal = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Normal", barcode = "111")
        val locked = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Locked", barcode = "222", isLocked = true)

        db.tagDao().insertAll(tag)
        dao.insertAll(normal, locked)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = normal.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = locked.id, tagId = tag.id))

        val counts = dao.getTagBarcodeCountsFiltered(
            showOnlyFavorites = false,
            showOnlyLocked = true,
            hideLocked = false,
            query = null
        ).first()

        assertEquals(1, counts.first { it.tagId == tag.id }.count)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getTagBarcodeCountsFiltered with search query should count only matching barcodes`() = runTest {
        val tag = Tag(id = 1, name = "Tag1", color = "#ff0000")
        val match = SavedBarcode(id = 1, date = Date(), type = 1, format = 1, title = "Coffee Shop", barcode = "111")
        val noMatch = SavedBarcode(id = 2, date = Date(), type = 1, format = 1, title = "Other", barcode = "222")

        db.tagDao().insertAll(tag)
        dao.insertAll(match, noMatch)
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = match.id, tagId = tag.id))
        dao.insertBarcodeTag(BarcodeTagCrossRef(barcodeId = noMatch.id, tagId = tag.id))

        val counts = dao.getTagBarcodeCountsFiltered(
            showOnlyFavorites = false,
            showOnlyLocked = false,
            hideLocked = false,
            query = "coffee"
        ).first()

        assertEquals(1, counts.first { it.tagId == tag.id }.count)
    }
}
