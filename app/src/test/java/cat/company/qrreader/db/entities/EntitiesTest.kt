package cat.company.qrreader.db.entities

import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Unit tests for database entity classes
 */
class EntitiesTest {

    // --- SavedBarcode tests ---

    /**
     * Test SavedBarcode creation with all parameters
     */
    @Test
    fun savedBarcode_creationWithAllParams_succeeds() {
        val date = Date()
        val barcode = SavedBarcode(
            id = 1,
            date = date,
            type = 5,
            format = 256,
            title = "Test Title",
            description = "Test Description",
            barcode = "test-barcode-content"
        )

        assertEquals(1, barcode.id)
        assertEquals(date, barcode.date)
        assertEquals(5, barcode.type)
        assertEquals(256, barcode.format)
        assertEquals("Test Title", barcode.title)
        assertEquals("Test Description", barcode.description)
        assertEquals("test-barcode-content", barcode.barcode)
    }

    /**
     * Test SavedBarcode creation with default values
     */
    @Test
    fun savedBarcode_creationWithDefaults_usesDefaultValues() {
        val barcode = SavedBarcode(
            type = 0,
            format = 0,
            barcode = "content"
        )

        assertEquals(0, barcode.id) // Default id is 0
        assertNotNull(barcode.date) // Default date is Date()
        assertNull(barcode.title) // Default title is null
        assertNull(barcode.description) // Default description is null
    }

    /**
     * Test SavedBarcode equality
     */
    @Test
    fun savedBarcode_equality_sameValues_areEqual() {
        val date = Date(1000000L)
        val barcode1 = SavedBarcode(
            id = 1,
            date = date,
            type = 5,
            format = 256,
            title = "Title",
            description = "Desc",
            barcode = "content"
        )
        val barcode2 = SavedBarcode(
            id = 1,
            date = date,
            type = 5,
            format = 256,
            title = "Title",
            description = "Desc",
            barcode = "content"
        )

        assertEquals(barcode1, barcode2)
    }

    /**
     * Test SavedBarcode inequality
     */
    @Test
    fun savedBarcode_inequality_differentIds_areNotEqual() {
        val barcode1 = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")
        val barcode2 = SavedBarcode(id = 2, type = 0, format = 0, barcode = "content")

        assertNotEquals(barcode1, barcode2)
    }

    /**
     * Test SavedBarcode copy function
     */
    @Test
    fun savedBarcode_copy_createsModifiedCopy() {
        val original = SavedBarcode(
            id = 1,
            type = 0,
            format = 0,
            title = "Original",
            barcode = "content"
        )
        val copied = original.copy(title = "Modified")

        assertEquals("Original", original.title)
        assertEquals("Modified", copied.title)
        assertEquals(original.id, copied.id)
    }

    /**
     * Test SavedBarcode title and description are mutable
     */
    @Test
    fun savedBarcode_mutableFields_canBeModified() {
        val barcode = SavedBarcode(type = 0, format = 0, barcode = "content")

        barcode.title = "New Title"
        barcode.description = "New Description"

        assertEquals("New Title", barcode.title)
        assertEquals("New Description", barcode.description)
    }

    /**
     * Test SavedBarcode hashCode consistency
     */
    @Test
    fun savedBarcode_hashCode_isConsistent() {
        val barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")

        val hash1 = barcode.hashCode()
        val hash2 = barcode.hashCode()

        assertEquals(hash1, hash2)
    }

    // --- Tag tests ---

    /**
     * Test Tag creation with all parameters
     */
    @Test
    fun tag_creationWithAllParams_succeeds() {
        val tag = Tag(
            id = 1,
            name = "Important",
            color = "#FF0000"
        )

        assertEquals(1, tag.id)
        assertEquals("Important", tag.name)
        assertEquals("#FF0000", tag.color)
    }

    /**
     * Test Tag creation with default id
     */
    @Test
    fun tag_creationWithDefaultId_usesZero() {
        val tag = Tag(name = "Work", color = "#00FF00")

        assertEquals(0, tag.id)
    }

    /**
     * Test Tag equality
     */
    @Test
    fun tag_equality_sameValues_areEqual() {
        val tag1 = Tag(id = 1, name = "Test", color = "#FFFFFF")
        val tag2 = Tag(id = 1, name = "Test", color = "#FFFFFF")

        assertEquals(tag1, tag2)
    }

    /**
     * Test Tag inequality
     */
    @Test
    fun tag_inequality_differentNames_areNotEqual() {
        val tag1 = Tag(id = 1, name = "Work", color = "#FFFFFF")
        val tag2 = Tag(id = 1, name = "Personal", color = "#FFFFFF")

        assertNotEquals(tag1, tag2)
    }

    /**
     * Test Tag copy function
     */
    @Test
    fun tag_copy_createsModifiedCopy() {
        val original = Tag(id = 1, name = "Original", color = "#000000")
        val copied = original.copy(color = "#FFFFFF")

        assertEquals("#000000", original.color)
        assertEquals("#FFFFFF", copied.color)
        assertEquals(original.name, copied.name)
    }

    /**
     * Test Tag with various color formats
     */
    @Test
    fun tag_colorFormats_acceptsVariousFormats() {
        // 6-digit hex
        val tag1 = Tag(name = "T1", color = "#FF0000")
        assertEquals("#FF0000", tag1.color)

        // 8-digit hex with alpha
        val tag2 = Tag(name = "T2", color = "#FFFF0000")
        assertEquals("#FFFF0000", tag2.color)

        // Lowercase hex
        val tag3 = Tag(name = "T3", color = "#ff00ff")
        assertEquals("#ff00ff", tag3.color)
    }

    // --- BarcodeTagCrossRef tests ---

    /**
     * Test BarcodeTagCrossRef creation
     */
    @Test
    fun barcodeTagCrossRef_creation_succeeds() {
        val crossRef = BarcodeTagCrossRef(barcodeId = 1, tagId = 2)

        assertEquals(1, crossRef.barcodeId)
        assertEquals(2, crossRef.tagId)
    }

    /**
     * Test BarcodeTagCrossRef equality
     */
    @Test
    fun barcodeTagCrossRef_equality_sameValues_areEqual() {
        val ref1 = BarcodeTagCrossRef(barcodeId = 1, tagId = 2)
        val ref2 = BarcodeTagCrossRef(barcodeId = 1, tagId = 2)

        assertEquals(ref1, ref2)
    }

    /**
     * Test BarcodeTagCrossRef inequality
     */
    @Test
    fun barcodeTagCrossRef_inequality_differentBarcodeId_areNotEqual() {
        val ref1 = BarcodeTagCrossRef(barcodeId = 1, tagId = 2)
        val ref2 = BarcodeTagCrossRef(barcodeId = 3, tagId = 2)

        assertNotEquals(ref1, ref2)
    }

    /**
     * Test BarcodeTagCrossRef inequality with different tagId
     */
    @Test
    fun barcodeTagCrossRef_inequality_differentTagId_areNotEqual() {
        val ref1 = BarcodeTagCrossRef(barcodeId = 1, tagId = 2)
        val ref2 = BarcodeTagCrossRef(barcodeId = 1, tagId = 3)

        assertNotEquals(ref1, ref2)
    }

    /**
     * Test BarcodeTagCrossRef copy function
     */
    @Test
    fun barcodeTagCrossRef_copy_createsModifiedCopy() {
        val original = BarcodeTagCrossRef(barcodeId = 1, tagId = 2)
        val copied = original.copy(tagId = 5)

        assertEquals(2, original.tagId)
        assertEquals(5, copied.tagId)
        assertEquals(original.barcodeId, copied.barcodeId)
    }

    // --- SavedBarcodeWithTags tests ---

    /**
     * Test SavedBarcodeWithTags creation with no tags
     */
    @Test
    fun savedBarcodeWithTags_creationWithNoTags_succeeds() {
        val barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")
        val barcodeWithTags = SavedBarcodeWithTags(barcode = barcode)

        assertEquals(barcode, barcodeWithTags.barcode)
        assertTrue(barcodeWithTags.tags.isEmpty())
    }

    /**
     * Test SavedBarcodeWithTags creation with tags
     */
    @Test
    fun savedBarcodeWithTags_creationWithTags_succeeds() {
        val barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")
        val tags = listOf(
            Tag(id = 1, name = "Work", color = "#FF0000"),
            Tag(id = 2, name = "Personal", color = "#00FF00")
        )
        val barcodeWithTags = SavedBarcodeWithTags(barcode = barcode, tags = tags)

        assertEquals(barcode, barcodeWithTags.barcode)
        assertEquals(2, barcodeWithTags.tags.size)
        assertEquals("Work", barcodeWithTags.tags[0].name)
        assertEquals("Personal", barcodeWithTags.tags[1].name)
    }

    /**
     * Test SavedBarcodeWithTags equality
     */
    @Test
    fun savedBarcodeWithTags_equality_sameValues_areEqual() {
        val barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")
        val tags = listOf(Tag(id = 1, name = "Test", color = "#FFFFFF"))

        val bwt1 = SavedBarcodeWithTags(barcode = barcode, tags = tags)
        val bwt2 = SavedBarcodeWithTags(barcode = barcode, tags = tags)

        assertEquals(bwt1, bwt2)
    }

    /**
     * Test SavedBarcodeWithTags default empty list
     */
    @Test
    fun savedBarcodeWithTags_defaultTags_isEmptyList() {
        val barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")
        val barcodeWithTags = SavedBarcodeWithTags(barcode = barcode)

        assertNotNull(barcodeWithTags.tags)
        assertEquals(emptyList<Tag>(), barcodeWithTags.tags)
    }

    /**
     * Test SavedBarcodeWithTags copy function
     */
    @Test
    fun savedBarcodeWithTags_copy_createsModifiedCopy() {
        val barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")
        val original = SavedBarcodeWithTags(barcode = barcode, tags = emptyList())
        val newTags = listOf(Tag(id = 1, name = "New", color = "#000000"))
        val copied = original.copy(tags = newTags)

        assertTrue(original.tags.isEmpty())
        assertEquals(1, copied.tags.size)
    }

    /**
     * Test SavedBarcodeWithTags accessing barcode properties
     */
    @Test
    fun savedBarcodeWithTags_accessingBarcodeProperties_works() {
        val barcode = SavedBarcode(
            id = 42,
            type = 5,
            format = 256,
            title = "Test",
            barcode = "content"
        )
        val barcodeWithTags = SavedBarcodeWithTags(barcode = barcode)

        assertEquals(42, barcodeWithTags.barcode.id)
        assertEquals(5, barcodeWithTags.barcode.type)
        assertEquals(256, barcodeWithTags.barcode.format)
        assertEquals("Test", barcodeWithTags.barcode.title)
        assertEquals("content", barcodeWithTags.barcode.barcode)
    }

    /**
     * Test multiple tags can be associated
     */
    @Test
    fun savedBarcodeWithTags_multipleTags_allAccessible() {
        val barcode = SavedBarcode(id = 1, type = 0, format = 0, barcode = "content")
        val tags = (1..5).map { Tag(id = it, name = "Tag$it", color = "#00000$it") }
        val barcodeWithTags = SavedBarcodeWithTags(barcode = barcode, tags = tags)

        assertEquals(5, barcodeWithTags.tags.size)
        tags.forEachIndexed { index, tag ->
            assertEquals(tag, barcodeWithTags.tags[index])
        }
    }
}
