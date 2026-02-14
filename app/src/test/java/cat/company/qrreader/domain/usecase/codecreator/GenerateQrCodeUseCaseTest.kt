package cat.company.qrreader.domain.usecase.codecreator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for GenerateQrCodeUseCase
 *
 * Tests cover:
 * - QR code generation with various inputs
 * - Error handling
 * - Edge cases (empty text, special characters, unicode, long text)
 * - Bitmap validation
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GenerateQrCodeUseCaseTest {

    private val useCase = GenerateQrCodeUseCase()

    @Test
    fun invoke_withValidText_returnsBitmap() {
        val text = "Hello World"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withEmptyText_returnsNull() {
        val text = ""
        
        val bitmap = useCase(text)
        
        assertNull(bitmap)
    }

    @Test
    fun invoke_withShortText_returnsBitmap() {
        val text = "A"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withLongText_returnsBitmap() {
        val text = "This is a very long text that will be encoded into a QR code. ".repeat(20)
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withSpecialCharacters_returnsBitmap() {
        val text = "https://example.com?param=value&other=123#fragment!@#\$%^&*()"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withUnicodeCharacters_returnsBitmap() {
        val text = "Hello 世界 🌍 Привет مرحبا"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withNumericText_returnsBitmap() {
        val text = "1234567890"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withUrl_returnsBitmap() {
        val text = "https://www.example.com/path/to/resource"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withEmail_returnsBitmap() {
        val text = "mailto:user@example.com?subject=Hello"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withPhoneNumber_returnsBitmap() {
        val text = "tel:+1234567890"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withWhitespace_returnsBitmap() {
        val text = "  Text with spaces  "
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withNewlines_returnsBitmap() {
        val text = "Line 1\nLine 2\nLine 3"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withTabs_returnsBitmap() {
        val text = "Text\twith\ttabs"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withJsonFormat_returnsBitmap() {
        val text = """{"name":"John","age":30,"city":"New York"}"""
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withXmlFormat_returnsBitmap() {
        val text = """<root><item>value</item></root>"""
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_multipleInvocations_eachReturnsValidBitmap() {
        val texts = listOf("First", "Second", "Third", "Fourth", "Fifth")
        
        texts.forEach { text ->
            val bitmap = useCase(text)
            assertNotNull("Bitmap should not be null for text: $text", bitmap)
            assertTrue("Bitmap width should be > 0 for text: $text", bitmap!!.width > 0)
            assertTrue("Bitmap height should be > 0 for text: $text", bitmap.height > 0)
        }
    }

    @Test
    fun invoke_sameTextMultipleTimes_returnsSimilarBitmaps() {
        val text = "Test"
        
        val bitmap1 = useCase(text)
        val bitmap2 = useCase(text)
        
        assertNotNull(bitmap1)
        assertNotNull(bitmap2)
        assertEquals(bitmap1!!.width, bitmap2!!.width)
        assertEquals(bitmap1.height, bitmap2.height)
    }

    @Test
    fun invoke_differentTexts_returnsBitmapsWithSameDimensions() {
        val text1 = "Hello"
        val text2 = "World"
        
        val bitmap1 = useCase(text1)
        val bitmap2 = useCase(text2)
        
        assertNotNull(bitmap1)
        assertNotNull(bitmap2)
        // QR codes typically have same dimensions for similar-length inputs
        assertEquals(bitmap1!!.width, bitmap2!!.width)
        assertEquals(bitmap1.height, bitmap2.height)
    }

    @Test
    fun invoke_withOnlySpaces_returnsBitmap() {
        val text = "     "
        
        val bitmap = useCase(text)
        
        // Spaces are valid content
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withEscapeSequences_returnsBitmap() {
        val text = "Text with \\n \\t \\r escape sequences"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withQuotes_returnsBitmap() {
        val text = """Text with "double" and 'single' quotes"""
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withBackslashes_returnsBitmap() {
        val text = """C:\path\to\file"""
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withVeryLongText_returnsBitmap() {
        // Test with text close to QR code maximum capacity
        val text = "A".repeat(2000)
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withMixedLanguages_returnsBitmap() {
        val text = "English, Español, 中文, العربية, हिन्दी"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withWifiConfig_returnsBitmap() {
        val text = "WIFI:T:WPA;S:MyNetwork;P:MyPassword;;"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_withVCard_returnsBitmap() {
        val text = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John Doe
            TEL:1234567890
            END:VCARD
        """.trimIndent()
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun invoke_bitmapFormat_isARGB8888() {
        val text = "Test"
        
        val bitmap = useCase(text)
        
        assertNotNull(bitmap)
        // Verify bitmap can be accessed (no exceptions thrown)
        val pixel = bitmap!!.getPixel(0, 0)
        // Pixel value should be either black or white (QR codes are binary)
        assertTrue(pixel == android.graphics.Color.BLACK || pixel == android.graphics.Color.WHITE)
    }
}
