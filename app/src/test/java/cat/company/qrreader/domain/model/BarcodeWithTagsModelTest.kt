package cat.company.qrreader.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Unit tests for the [BarcodeWithTagsModel] domain model.
 */
class BarcodeWithTagsModelTest {

    private fun sampleBarcode(id: Int = 1) = BarcodeModel(
        id = id,
        date = Date(1700000000000L),
        type = 5,
        format = 256,
        barcode = "https://example.com"
    )

    private fun sampleTag(id: Int = 1, name: String = "Work") =
        TagModel(id = id, name = name, color = "#2196F3")

    @Test
    fun `creation with barcode and tags`() {
        val barcode = sampleBarcode()
        val tags = listOf(sampleTag(1, "Work"), sampleTag(2, "Travel"))
        val model = BarcodeWithTagsModel(barcode = barcode, tags = tags)

        assertEquals(barcode, model.barcode)
        assertEquals(2, model.tags.size)
        assertEquals("Work", model.tags[0].name)
        assertEquals("Travel", model.tags[1].name)
    }

    @Test
    fun `creation with empty tags`() {
        val model = BarcodeWithTagsModel(barcode = sampleBarcode(), tags = emptyList())

        assertTrue(model.tags.isEmpty())
    }

    @Test
    fun `equality for same content`() {
        val barcode = sampleBarcode()
        val tags = listOf(sampleTag())
        val a = BarcodeWithTagsModel(barcode = barcode, tags = tags)
        val b = BarcodeWithTagsModel(barcode = barcode, tags = tags)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality for different barcodes`() {
        val tags = listOf(sampleTag())
        val a = BarcodeWithTagsModel(barcode = sampleBarcode(1), tags = tags)
        val b = BarcodeWithTagsModel(barcode = sampleBarcode(2), tags = tags)

        assertNotEquals(a, b)
    }

    @Test
    fun `inequality for different tags`() {
        val barcode = sampleBarcode()
        val a = BarcodeWithTagsModel(barcode = barcode, tags = listOf(sampleTag(1, "Work")))
        val b = BarcodeWithTagsModel(barcode = barcode, tags = listOf(sampleTag(2, "Travel")))

        assertNotEquals(a, b)
    }

    @Test
    fun `copy changes only barcode`() {
        val original = BarcodeWithTagsModel(barcode = sampleBarcode(1), tags = listOf(sampleTag()))
        val newBarcode = sampleBarcode(2)
        val copied = original.copy(barcode = newBarcode)

        assertEquals(newBarcode, copied.barcode)
        assertEquals(original.tags, copied.tags)
    }

    @Test
    fun `copy changes only tags`() {
        val original = BarcodeWithTagsModel(barcode = sampleBarcode(), tags = listOf(sampleTag(1, "Work")))
        val newTags = listOf(sampleTag(2, "Travel"), sampleTag(3, "Health"))
        val copied = original.copy(tags = newTags)

        assertEquals(original.barcode, copied.barcode)
        assertEquals(newTags, copied.tags)
    }
}

