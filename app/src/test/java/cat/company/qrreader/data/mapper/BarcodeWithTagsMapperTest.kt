package cat.company.qrreader.data.mapper

import cat.company.qrreader.db.entities.SavedBarcode
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Unit tests for BarcodeWithTagsMapper extension functions.
 *
 * Verifies correct mapping of [SavedBarcodeWithTags] compound entity
 * to [cat.company.qrreader.domain.model.BarcodeWithTagsModel] domain model.
 */
class BarcodeWithTagsMapperTest {

    @Test
    fun `toDomainModel maps barcode and tags correctly`() {
        val date = Date(1700000000000L)
        val entity = SavedBarcodeWithTags(
            barcode = SavedBarcode(
                id = 1,
                date = date,
                type = 5,
                format = 256,
                title = "Test",
                description = "Desc",
                barcode = "https://example.com",
                aiGeneratedDescription = "An example site.",
                isFavorite = true
            ),
            tags = listOf(
                Tag(id = 10, name = "Work", color = "#2196F3"),
                Tag(id = 20, name = "Travel", color = "#4CAF50")
            )
        )

        val model = entity.toDomainModel()

        assertEquals(1, model.barcode.id)
        assertEquals(date, model.barcode.date)
        assertEquals("https://example.com", model.barcode.barcode)
        assertEquals("An example site.", model.barcode.aiGeneratedDescription)
        assertTrue(model.barcode.isFavorite)
        assertEquals(2, model.tags.size)
        assertEquals("Work", model.tags[0].name)
        assertEquals("#2196F3", model.tags[0].color)
        assertEquals("Travel", model.tags[1].name)
    }

    @Test
    fun `toDomainModel handles empty tags list`() {
        val entity = SavedBarcodeWithTags(
            barcode = SavedBarcode(
                type = 0,
                format = 0,
                barcode = "content"
            ),
            tags = emptyList()
        )

        val model = entity.toDomainModel()

        assertTrue(model.tags.isEmpty())
        assertEquals("content", model.barcode.barcode)
    }

    @Test
    fun `toDomainModel handles default tags list`() {
        val entity = SavedBarcodeWithTags(
            barcode = SavedBarcode(
                type = 1,
                format = 1,
                barcode = "data"
            )
        )

        val model = entity.toDomainModel()

        assertTrue(model.tags.isEmpty())
    }

    @Test
    fun `toDomainModel maps many tags correctly`() {
        val tags = (1..10).map { Tag(id = it, name = "Tag$it", color = "#AABBCC") }
        val entity = SavedBarcodeWithTags(
            barcode = SavedBarcode(type = 0, format = 0, barcode = "x"),
            tags = tags
        )

        val model = entity.toDomainModel()

        assertEquals(10, model.tags.size)
        model.tags.forEachIndexed { index, tagModel ->
            assertEquals("Tag${index + 1}", tagModel.name)
            assertEquals(index + 1, tagModel.id)
        }
    }
}

