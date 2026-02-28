package cat.company.qrreader.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for the [TagModel] domain model.
 */
class TagModelTest {

    @Test
    fun `creation with all fields`() {
        val model = TagModel(id = 5, name = "Shopping", color = "#FF5722")

        assertEquals(5, model.id)
        assertEquals("Shopping", model.name)
        assertEquals("#FF5722", model.color)
    }

    @Test
    fun `default id is zero`() {
        val model = TagModel(name = "Work", color = "#2196F3")

        assertEquals(0, model.id)
    }

    @Test
    fun `equality for same values`() {
        val a = TagModel(id = 1, name = "Travel", color = "#4CAF50")
        val b = TagModel(id = 1, name = "Travel", color = "#4CAF50")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality for different names`() {
        val a = TagModel(id = 1, name = "Travel", color = "#4CAF50")
        val b = TagModel(id = 1, name = "Work", color = "#4CAF50")

        assertNotEquals(a, b)
    }

    @Test
    fun `inequality for different colors`() {
        val a = TagModel(id = 1, name = "Travel", color = "#4CAF50")
        val b = TagModel(id = 1, name = "Travel", color = "#FF0000")

        assertNotEquals(a, b)
    }

    @Test
    fun `inequality for different ids`() {
        val a = TagModel(id = 1, name = "Travel", color = "#4CAF50")
        val b = TagModel(id = 2, name = "Travel", color = "#4CAF50")

        assertNotEquals(a, b)
    }

    @Test
    fun `copy changes only specified fields`() {
        val original = TagModel(id = 3, name = "Health", color = "#E91E63")
        val copied = original.copy(name = "Fitness")

        assertEquals("Fitness", copied.name)
        assertEquals(original.id, copied.id)
        assertEquals(original.color, copied.color)
        assertEquals("Health", original.name) // original unchanged
    }

    @Test
    fun `handles empty name`() {
        val model = TagModel(name = "", color = "#000000")

        assertEquals("", model.name)
    }

    @Test
    fun `handles long color string`() {
        val model = TagModel(name = "Test", color = "#FF2196F3")

        assertEquals("#FF2196F3", model.color)
    }
}

