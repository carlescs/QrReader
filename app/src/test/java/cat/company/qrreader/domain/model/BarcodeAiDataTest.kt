package cat.company.qrreader.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the BarcodeAiData domain model.
 */
class BarcodeAiDataTest {

    @Test
    fun `BarcodeAiData stores tags and description`() {
        val tags = listOf(SuggestedTagModel("Shopping", true))
        val data = BarcodeAiData(tags = tags, description = "A shopping website.")

        assertEquals(tags, data.tags)
        assertEquals("A shopping website.", data.description)
    }

    @Test
    fun `BarcodeAiData with empty tags is valid`() {
        val data = BarcodeAiData(tags = emptyList(), description = "No tags.")

        assertTrue(data.tags.isEmpty())
        assertEquals("No tags.", data.description)
    }

    @Test
    fun `BarcodeAiData with empty description is valid`() {
        val tags = listOf(SuggestedTagModel("Work", true))
        val data = BarcodeAiData(tags = tags, description = "")

        assertEquals(tags, data.tags)
        assertTrue(data.description.isEmpty())
    }

    @Test
    fun `BarcodeAiData equality holds for same content`() {
        val color = "#FF0000"
        val tags = listOf(SuggestedTagModel("Travel", true, color))
        val data1 = BarcodeAiData(tags = tags, description = "A travel site.")
        val data2 = BarcodeAiData(tags = tags, description = "A travel site.")

        assertEquals(data1, data2)
    }

    @Test
    fun `BarcodeAiData inequality for different descriptions`() {
        val tags = listOf(SuggestedTagModel("Work", true, "#AABBCC"))
        val data1 = BarcodeAiData(tags = tags, description = "First description.")
        val data2 = BarcodeAiData(tags = tags, description = "Second description.")

        assertNotEquals(data1, data2)
    }

    @Test
    fun `BarcodeAiData inequality for different tags`() {
        val data1 = BarcodeAiData(tags = listOf(SuggestedTagModel("Work", true, "#AABBCC")), description = "Same.")
        val data2 = BarcodeAiData(tags = listOf(SuggestedTagModel("Travel", true, "#AABBCC")), description = "Same.")

        assertNotEquals(data1, data2)
    }

    @Test
    fun `BarcodeAiData copy changes description`() {
        val tags = listOf(SuggestedTagModel("Finance", true))
        val original = BarcodeAiData(tags = tags, description = "Original description.")
        val copied = original.copy(description = "Updated description.")

        assertEquals(tags, copied.tags)
        assertEquals("Updated description.", copied.description)
        assertEquals("Original description.", original.description)
    }

    @Test
    fun `BarcodeAiData copy changes tags`() {
        val originalTags = listOf(SuggestedTagModel("Finance", true))
        val newTags = listOf(SuggestedTagModel("Shopping", true), SuggestedTagModel("Online", true))
        val original = BarcodeAiData(tags = originalTags, description = "A barcode.")
        val copied = original.copy(tags = newTags)

        assertEquals(newTags, copied.tags)
        assertEquals("A barcode.", copied.description)
    }

    @Test
    fun `BarcodeAiData supports up to 3 tags`() {
        val tags = listOf(
            SuggestedTagModel("Tag1", true),
            SuggestedTagModel("Tag2", true),
            SuggestedTagModel("Tag3", true)
        )
        val data = BarcodeAiData(tags = tags, description = "Three tags.")

        assertEquals(3, data.tags.size)
    }

    @Test
    fun `BarcodeAiData hashCode consistent with equality`() {
        val color = "#CCDDEE"
        val tags = listOf(SuggestedTagModel("Health", true, color))
        val data1 = BarcodeAiData(tags = tags, description = "Health barcode.")
        val data2 = BarcodeAiData(tags = tags, description = "Health barcode.")

        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun `BarcodeAiData tags are not selected by default`() {
        val tags = listOf(
            SuggestedTagModel("Tag1"),
            SuggestedTagModel("Tag2")
        )
        val data = BarcodeAiData(tags = tags, description = "Default selection test.")

        assertTrue(data.tags.all { !it.isSelected })
    }

    @Test
    fun `BarcodeAiData toString contains class name`() {
        val data = BarcodeAiData(tags = emptyList(), description = "Test.")

        assertTrue(data.toString().contains("BarcodeAiData"))
        assertFalse(data.toString().isEmpty())
    }
}
