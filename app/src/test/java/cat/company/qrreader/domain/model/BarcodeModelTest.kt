package cat.company.qrreader.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Unit tests for the [BarcodeModel] domain model.
 */
class BarcodeModelTest {

    @Test
    fun `creation with all fields`() {
        val date = Date(1700000000000L)
        val model = BarcodeModel(
            id = 1,
            date = date,
            type = 5,
            format = 256,
            title = "Title",
            description = "Desc",
            barcode = "content",
            aiGeneratedDescription = "AI desc",
            isFavorite = true
        )

        assertEquals(1, model.id)
        assertEquals(date, model.date)
        assertEquals(5, model.type)
        assertEquals(256, model.format)
        assertEquals("Title", model.title)
        assertEquals("Desc", model.description)
        assertEquals("content", model.barcode)
        assertEquals("AI desc", model.aiGeneratedDescription)
        assertTrue(model.isFavorite)
    }

    @Test
    fun `default values are applied correctly`() {
        val model = BarcodeModel(type = 0, format = 0, barcode = "test")

        assertEquals(0, model.id)
        assertNotNull(model.date)
        assertNull(model.title)
        assertNull(model.description)
        assertNull(model.aiGeneratedDescription)
        assertFalse(model.isFavorite)
    }

    @Test
    fun `equality for same values`() {
        val date = Date(1000L)
        val a = BarcodeModel(id = 1, date = date, type = 0, format = 0, barcode = "x")
        val b = BarcodeModel(id = 1, date = date, type = 0, format = 0, barcode = "x")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality for different ids`() {
        val date = Date(1000L)
        val a = BarcodeModel(id = 1, date = date, type = 0, format = 0, barcode = "x")
        val b = BarcodeModel(id = 2, date = date, type = 0, format = 0, barcode = "x")

        assertNotEquals(a, b)
    }

    @Test
    fun `inequality for different barcode content`() {
        val date = Date(1000L)
        val a = BarcodeModel(id = 1, date = date, type = 0, format = 0, barcode = "x")
        val b = BarcodeModel(id = 1, date = date, type = 0, format = 0, barcode = "y")

        assertNotEquals(a, b)
    }

    @Test
    fun `inequality for different isFavorite`() {
        val date = Date(1000L)
        val a = BarcodeModel(id = 1, date = date, type = 0, format = 0, barcode = "x", isFavorite = true)
        val b = BarcodeModel(id = 1, date = date, type = 0, format = 0, barcode = "x", isFavorite = false)

        assertNotEquals(a, b)
    }

    @Test
    fun `copy changes only specified fields`() {
        val original = BarcodeModel(
            id = 1, type = 5, format = 256, barcode = "data",
            title = "Old", isFavorite = false
        )
        val copied = original.copy(title = "New", isFavorite = true)

        assertEquals("New", copied.title)
        assertTrue(copied.isFavorite)
        assertEquals(original.id, copied.id)
        assertEquals(original.barcode, copied.barcode)
        assertEquals("Old", original.title) // original unchanged
    }

    @Test
    fun `copy preserves all fields when no changes specified`() {
        val date = Date(5000L)
        val original = BarcodeModel(
            id = 10, date = date, type = 3, format = 64, barcode = "abc",
            title = "T", description = "D", aiGeneratedDescription = "AI", isFavorite = true
        )
        val copied = original.copy()

        assertEquals(original, copied)
    }
}

