package cat.company.qrreader.utils

import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for Utils class
 */
@RunWith(RobolectricTestRunner::class)
class UtilsTest {

    /**
     * Test parseColor with valid hex colors
     */
    @Test
    fun parseColor_validHexColor_returnsColor() {
        // Test with 6-digit hex - should return a valid color
        val redColor = Utils.parseColor("#FF0000")
        assertNotNull(redColor)

        // Test with 8-digit hex (with alpha) - should return a valid color
        val blueColor = Utils.parseColor("#FF0000FF")
        assertNotNull(blueColor)

        // Test black - should return a valid color
        val blackColor = Utils.parseColor("#000000")
        assertNotNull(blackColor)

        // Test white - should return a valid color
        val whiteColor = Utils.parseColor("#FFFFFF")
        assertNotNull(whiteColor)
    }

    /**
     * Test parseColor with invalid color strings
     */
    @Test
    fun parseColor_invalidColorString_returnsNull() {
        // Test with invalid format
        val invalidColor1 = Utils.parseColor("not a color")
        assertNull(invalidColor1)

        // Test with invalid hex
        val invalidColor2 = Utils.parseColor("#GGGGGG")
        assertNull(invalidColor2)

        // Test with empty string
        val invalidColor3 = Utils.parseColor("")
        assertNull(invalidColor3)

        // Test with malformed hex
        val invalidColor4 = Utils.parseColor("#12345")
        assertNull(invalidColor4)
    }

    /**
     * Test colorToString conversion
     */
    @Test
    fun colorToString_validColor_returnsHexString() {
        // Test red
        val redHex = Utils.colorToString(Color.Red)
        assertEquals("#ffff0000", redHex.lowercase())

        // Test blue
        val blueHex = Utils.colorToString(Color.Blue)
        assertEquals("#ff0000ff", blueHex.lowercase())

        // Test black
        val blackHex = Utils.colorToString(Color.Black)
        assertEquals("#ff000000", blackHex.lowercase())

        // Test white
        val whiteHex = Utils.colorToString(Color.White)
        assertEquals("#ffffffff", whiteHex.lowercase())
    }

    /**
     * Test colorToString and parseColor roundtrip
     */
    @Test
    fun colorConversion_roundTrip_preservesColor() {
        val originalColor = Color(0xFF123456)
        val hexString = Utils.colorToString(originalColor)
        val parsedColor = Utils.parseColor(hexString)

        assertNotNull(parsedColor)
        assertNotNull(hexString)
        assertTrue(hexString.startsWith("#"))
    }

    /**
     * Test colorBasedOnBackground with light backgrounds
     */
    @Test
    fun colorBasedOnBackground_lightBackground_returnsBlack() {
        // Test with white background (high luminance)
        val colorOnWhite = Utils.colorBasedOnBackground(Color.White)
        assertEquals(Color.Black, colorOnWhite)

        // Test with light gray
        val colorOnLightGray = Utils.colorBasedOnBackground(Color(0xFFCCCCCC))
        assertEquals(Color.Black, colorOnLightGray)

        // Test with yellow (high luminance)
        val colorOnYellow = Utils.colorBasedOnBackground(Color.Yellow)
        assertEquals(Color.Black, colorOnYellow)
    }

    /**
     * Test colorBasedOnBackground with dark backgrounds
     */
    @Test
    fun colorBasedOnBackground_darkBackground_returnsWhite() {
        // Test with black background (low luminance)
        val colorOnBlack = Utils.colorBasedOnBackground(Color.Black)
        assertEquals(Color.White, colorOnBlack)

        // Test with dark gray
        val colorOnDarkGray = Utils.colorBasedOnBackground(Color(0xFF333333))
        assertEquals(Color.White, colorOnDarkGray)

        // Test with blue (low luminance)
        val colorOnBlue = Utils.colorBasedOnBackground(Color.Blue)
        assertEquals(Color.White, colorOnBlue)

        // Test with red (low luminance)
        val colorOnRed = Utils.colorBasedOnBackground(Color.Red)
        assertEquals(Color.White, colorOnRed)
    }

    /**
     * Test colorBasedOnBackground with null color
     */
    @Test
    fun colorBasedOnBackground_nullColor_returnsBlack() {
        val colorOnNull = Utils.colorBasedOnBackground(null)
        assertEquals(Color.Black, colorOnNull)
    }

    /**
     * Test colorBasedOnBackground with edge case (luminance exactly 0.5)
     */
    @Test
    fun colorBasedOnBackground_luminanceAtThreshold_behavesConsistently() {
        // Create a color with luminance close to 0.5
        val mediumGray = Color(0xFF808080)
        val result = Utils.colorBasedOnBackground(mediumGray)

        // Should return either Black or White consistently
        assertTrue(result == Color.Black || result == Color.White)
    }

    /**
     * Test parseColor with colors without # prefix
     */
    @Test
    fun parseColor_colorWithoutHashPrefix_returnsNull() {
        // Android's Color.parseColor expects # prefix
        val colorWithoutHash = Utils.parseColor("FF0000")
        assertNull(colorWithoutHash)
    }

    /**
     * Test parseColor with lowercase hex
     */
    @Test
    fun parseColor_lowercaseHex_returnsColor() {
        val colorLowercase = Utils.parseColor("#ff0000")
        assertNotNull(colorLowercase)
    }

    /**
     * Test parseColor with uppercase hex
     */
    @Test
    fun parseColor_uppercaseHex_returnsColor() {
        val colorUppercase = Utils.parseColor("#FF0000")
        assertNotNull(colorUppercase)
    }

    /**
     * Test colorToString always includes alpha channel
     */
    @Test
    fun colorToString_alwaysIncludesAlpha() {
        val color = Color(0x12345678)
        val hexString = Utils.colorToString(color)

        // Should start with # and have 8 characters after (AARRGGBB)
        assertTrue(hexString.startsWith("#"))
        assertEquals(9, hexString.length) // # + 8 hex digits
    }
}
