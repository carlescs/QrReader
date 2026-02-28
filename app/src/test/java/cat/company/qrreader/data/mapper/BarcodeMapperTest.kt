package cat.company.qrreader.data.mapper

import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.domain.model.BarcodeModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Date

/**
 * Unit tests for BarcodeMapper extension functions.
 *
 * Verifies correct mapping between [SavedBarcode] (entity) and [BarcodeModel] (domain)
 * in both directions, including nullable fields, defaults, and edge cases.
 */
class BarcodeMapperTest {

    // ── toDomainModel() ──────────────────────────────────────────────────────

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val date = Date(1700000000000L)
        val entity = SavedBarcode(
            id = 42,
            date = date,
            type = 5,
            format = 256,
            title = "My Title",
            description = "My Description",
            barcode = "https://example.com",
            aiGeneratedDescription = "An example website.",
            isFavorite = true
        )

        val model = entity.toDomainModel()

        assertEquals(42, model.id)
        assertEquals(date, model.date)
        assertEquals(5, model.type)
        assertEquals(256, model.format)
        assertEquals("My Title", model.title)
        assertEquals("My Description", model.description)
        assertEquals("https://example.com", model.barcode)
        assertEquals("An example website.", model.aiGeneratedDescription)
        assertTrue(model.isFavorite)
    }

    @Test
    fun `toDomainModel preserves null optional fields`() {
        val entity = SavedBarcode(
            id = 1,
            type = 0,
            format = 0,
            barcode = "content"
        )

        val model = entity.toDomainModel()

        assertNull(model.title)
        assertNull(model.description)
        assertNull(model.aiGeneratedDescription)
        assertFalse(model.isFavorite)
    }

    @Test
    fun `toDomainModel handles default id zero`() {
        val entity = SavedBarcode(type = 3, format = 1, barcode = "data")
        val model = entity.toDomainModel()

        assertEquals(0, model.id)
    }

    @Test
    fun `toDomainModel handles empty barcode content`() {
        val entity = SavedBarcode(type = 0, format = 0, barcode = "")
        val model = entity.toDomainModel()

        assertEquals("", model.barcode)
    }

    // ── toEntity() ───────────────────────────────────────────────────────────

    @Test
    fun `toEntity maps all fields correctly`() {
        val date = Date(1700000000000L)
        val model = BarcodeModel(
            id = 99,
            date = date,
            type = 7,
            format = 512,
            title = "Title",
            description = "Desc",
            barcode = "tel:+34123456789",
            aiGeneratedDescription = "A Spanish phone number.",
            isFavorite = true
        )

        val entity = model.toEntity()

        assertEquals(99, entity.id)
        assertEquals(date, entity.date)
        assertEquals(7, entity.type)
        assertEquals(512, entity.format)
        assertEquals("Title", entity.title)
        assertEquals("Desc", entity.description)
        assertEquals("tel:+34123456789", entity.barcode)
        assertEquals("A Spanish phone number.", entity.aiGeneratedDescription)
        assertTrue(entity.isFavorite)
    }

    @Test
    fun `toEntity preserves null optional fields`() {
        val model = BarcodeModel(type = 0, format = 0, barcode = "x")
        val entity = model.toEntity()

        assertNull(entity.title)
        assertNull(entity.description)
        assertNull(entity.aiGeneratedDescription)
        assertFalse(entity.isFavorite)
    }

    // ── Round-trip ───────────────────────────────────────────────────────────

    @Test
    fun `entity to model to entity round-trip preserves all data`() {
        val date = Date(1609459200000L)
        val original = SavedBarcode(
            id = 10,
            date = date,
            type = 2,
            format = 128,
            title = "Round Trip",
            description = "Testing round trip",
            barcode = "WIFI:T:WPA;S:Net;P:pass;;",
            aiGeneratedDescription = "A WiFi network.",
            isFavorite = true
        )

        val roundTripped = original.toDomainModel().toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `model to entity to model round-trip preserves all data`() {
        val date = Date(1609459200000L)
        val original = BarcodeModel(
            id = 20,
            date = date,
            type = 4,
            format = 64,
            title = null,
            description = null,
            barcode = "BEGIN:VCARD\nFN:Test\nEND:VCARD",
            aiGeneratedDescription = null,
            isFavorite = false
        )

        val roundTripped = original.toEntity().toDomainModel()

        assertEquals(original, roundTripped)
    }
}

