package cat.company.qrreader.domain.usecase.barcode

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for GenerateBarcodeDescriptionUseCase
 * 
 * Uses Robolectric to handle Android framework dependencies (Log, etc.)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GenerateBarcodeDescriptionUseCaseTest {
    @Test
    fun `invoke returns unavailable on device without Gemini Nano`() = runTest {
        // This test will likely fail on CI since Gemini Nano won't be available
        // But demonstrates the structure for testing
        val useCase = GenerateBarcodeDescriptionUseCase()
        
        val result = useCase(
            barcodeContent = "https://example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code"
        )
        
        // Expected to fail with UnsupportedOperationException or model unavailable
        assertTrue(result.isFailure)
    }

    @Test
    fun parseDescriptionText_handlesCodeFenceJson() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val raw = """
            ```json
            {"description": "Example site with tutorials."}
            ```
        """.trimIndent()

        val description = useCase.parseDescriptionText(raw)

        assertEquals("Example site with tutorials.", description)
    }

    @Test
    fun parseDescriptionText_fallsBackToRawText() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val raw = "Plain text response without JSON"

        val description = useCase.parseDescriptionText(raw)

        assertEquals("Plain text response without JSON", description)
    }

    @Test
    fun parseDescriptionText_handlesEmptyResponse() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val raw = ""

        val description = useCase.parseDescriptionText(raw)

        assertEquals("", description)
    }

    @Test
    fun parseDescriptionText_handlesWhitespaceOnlyResponse() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val raw = "   \n  "

        val description = useCase.parseDescriptionText(raw)

        assertEquals("", description)
    }

    @Test
    fun parseDescriptionText_handlesJsonWithPreambleAndFence() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val raw = """
            Here's your result:
            ```json
            {"description":"Example"}
            ```
        """.trimIndent()

        val description = useCase.parseDescriptionText(raw)

        assertEquals("Example", description)
    }

    @Test
    fun parseDescriptionText_handlesJsonWithPreambleWithoutFence() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val raw = "Sure! Here's the description: {\"description\":\"Example\"}"

        val description = useCase.parseDescriptionText(raw)

        assertEquals("Example", description)
    }

    @Test
    fun parseDescriptionText_handlesMalformedJsonInsideFence() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val raw = """
            ```json
            {"description": "Example"
            ```
        """.trimIndent()

        val description = useCase.parseDescriptionText(raw)

        // Malformed JSON should cause a fallback to the raw text
        assertEquals(raw, description)
    }

    @Test
    fun parseDescriptionText_truncatesLongDescription() = runTest {
        val useCase = GenerateBarcodeDescriptionUseCase()
        val longText = "A".repeat(250)
        val raw = """
            ```json
            {"description":"$longText"}
            ```
        """.trimIndent()

        val description = useCase.parseDescriptionText(raw)

        // Description should be truncated to the maximum allowed length (200 chars)
        assertEquals(200, description.length)
        assertEquals("A".repeat(200), description)
    }
}
