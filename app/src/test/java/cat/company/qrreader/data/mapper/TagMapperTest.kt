package cat.company.qrreader.data.mapper

import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.domain.model.TagModel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for TagMapper extension functions.
 *
 * Verifies correct mapping between [Tag] (entity) and [TagModel] (domain)
 * in both directions.
 */
class TagMapperTest {

    // ── toDomainModel() ──────────────────────────────────────────────────────

    @Test
    fun `toDomainModel maps all fields correctly`() {
        val entity = Tag(id = 5, name = "Shopping", color = "#FF5722")

        val model = entity.toDomainModel()

        assertEquals(5, model.id)
        assertEquals("Shopping", model.name)
        assertEquals("#FF5722", model.color)
    }

    @Test
    fun `toDomainModel handles default id zero`() {
        val entity = Tag(name = "Work", color = "#2196F3")
        val model = entity.toDomainModel()

        assertEquals(0, model.id)
    }

    @Test
    fun `toDomainModel handles empty name`() {
        val entity = Tag(id = 1, name = "", color = "#000000")
        val model = entity.toDomainModel()

        assertEquals("", model.name)
    }

    // ── toEntity() ───────────────────────────────────────────────────────────

    @Test
    fun `toEntity maps all fields correctly`() {
        val model = TagModel(id = 10, name = "Travel", color = "#4CAF50")

        val entity = model.toEntity()

        assertEquals(10, entity.id)
        assertEquals("Travel", entity.name)
        assertEquals("#4CAF50", entity.color)
    }

    @Test
    fun `toEntity handles default id zero`() {
        val model = TagModel(name = "Finance", color = "#9C27B0")
        val entity = model.toEntity()

        assertEquals(0, entity.id)
    }

    // ── Round-trip ───────────────────────────────────────────────────────────

    @Test
    fun `entity to model to entity round-trip preserves all data`() {
        val original = Tag(id = 7, name = "Health", color = "#E91E63")

        val roundTripped = original.toDomainModel().toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `model to entity to model round-trip preserves all data`() {
        val original = TagModel(id = 15, name = "Education", color = "#00BCD4")

        val roundTripped = original.toEntity().toDomainModel()

        assertEquals(original, roundTripped)
    }
}

