package cat.company.qrreader.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the [SuggestedTagModel] domain model.
 */
class SuggestedTagModelTest {

    @Test
    fun `creation with all fields`() {
        val model = SuggestedTagModel(name = "Shopping", isSelected = true, color = "#FF5722")

        assertEquals("Shopping", model.name)
        assertTrue(model.isSelected)
        assertEquals("#FF5722", model.color)
    }

    @Test
    fun `default isSelected is false`() {
        val model = SuggestedTagModel(name = "Work", color = "#2196F3")

        assertFalse(model.isSelected)
    }

    @Test
    fun `default color is a generated random color`() {
        val model = SuggestedTagModel(name = "Test")

        // Default color should be a valid hex color string
        assertTrue(model.color.startsWith("#"))
        assertEquals(7, model.color.length) // #RRGGBB format
    }

    @Test
    fun `equality for same values`() {
        val color = "#AABBCC"
        val a = SuggestedTagModel(name = "Work", isSelected = true, color = color)
        val b = SuggestedTagModel(name = "Work", isSelected = true, color = color)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `inequality for different names`() {
        val color = "#AABBCC"
        val a = SuggestedTagModel(name = "Work", isSelected = true, color = color)
        val b = SuggestedTagModel(name = "Travel", isSelected = true, color = color)

        assertNotEquals(a, b)
    }

    @Test
    fun `inequality for different isSelected`() {
        val color = "#AABBCC"
        val a = SuggestedTagModel(name = "Work", isSelected = true, color = color)
        val b = SuggestedTagModel(name = "Work", isSelected = false, color = color)

        assertNotEquals(a, b)
    }

    @Test
    fun `inequality for different colors`() {
        val a = SuggestedTagModel(name = "Work", isSelected = true, color = "#AABBCC")
        val b = SuggestedTagModel(name = "Work", isSelected = true, color = "#112233")

        assertNotEquals(a, b)
    }

    @Test
    fun `copy toggles selection`() {
        val original = SuggestedTagModel(name = "Finance", isSelected = false, color = "#FF0000")
        val toggled = original.copy(isSelected = true)

        assertTrue(toggled.isSelected)
        assertFalse(original.isSelected)
        assertEquals(original.name, toggled.name)
        assertEquals(original.color, toggled.color)
    }

    @Test
    fun `copy changes name preserves other fields`() {
        val original = SuggestedTagModel(name = "Old", isSelected = true, color = "#00FF00")
        val renamed = original.copy(name = "New")

        assertEquals("New", renamed.name)
        assertTrue(renamed.isSelected)
        assertEquals("#00FF00", renamed.color)
    }

    // ── generateRandomColor() ────────────────────────────────────────────────

    @Test
    fun `generateRandomColor returns valid hex format`() {
        val color = SuggestedTagModel.generateRandomColor()

        assertTrue("Color should start with #", color.startsWith("#"))
        assertEquals("Color should be #RRGGBB (7 chars)", 7, color.length)
    }

    @Test
    fun `generateRandomColor returns parseable hex color`() {
        val color = SuggestedTagModel.generateRandomColor()
        val hex = color.removePrefix("#")

        // Should be valid hex
        val value = hex.toLongOrNull(16)
        assertTrue("Color should be a valid hex number", value != null)
    }

    @Test
    fun `generateRandomColor produces pastel colors in 128-255 range`() {
        // Run multiple times to increase confidence
        repeat(50) {
            val color = SuggestedTagModel.generateRandomColor()
            val hex = color.removePrefix("#")
            val red = hex.substring(0, 2).toInt(16)
            val green = hex.substring(2, 4).toInt(16)
            val blue = hex.substring(4, 6).toInt(16)

            assertTrue("Red ($red) should be >= 128", red >= 128)
            assertTrue("Red ($red) should be <= 255", red <= 255)
            assertTrue("Green ($green) should be >= 128", green >= 128)
            assertTrue("Green ($green) should be <= 255", green <= 255)
            assertTrue("Blue ($blue) should be >= 128", blue >= 128)
            assertTrue("Blue ($blue) should be <= 255", blue <= 255)
        }
    }

    @Test
    fun `generateRandomColor produces varying colors`() {
        val colors = (1..20).map { SuggestedTagModel.generateRandomColor() }.toSet()

        // With 20 random pastel colors, we should get at least 2 unique ones
        assertTrue("Should generate varying colors, got ${colors.size}", colors.size >= 2)
    }
}

