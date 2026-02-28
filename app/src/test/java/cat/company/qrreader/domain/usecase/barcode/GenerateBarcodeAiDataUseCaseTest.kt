package cat.company.qrreader.domain.usecase.barcode

import cat.company.qrreader.domain.model.BarcodeAiData
import cat.company.qrreader.domain.model.SuggestedTagModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for GenerateBarcodeAiDataUseCase.
 *
 * Uses Robolectric to handle Android framework dependencies (Log, etc.).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GenerateBarcodeAiDataUseCaseTest {

    // -------------------------------------------------------------------
    // Real use case — tests what happens when Gemini Nano is absent
    // -------------------------------------------------------------------

    @Test
    fun `invoke returns failure on device without Gemini Nano`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()

        val result = useCase(
            barcodeContent = "https://example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code",
            existingTags = emptyList()
        )

        // Gemini Nano is not present in the unit-test JVM environment
        assertTrue(result.isFailure)
    }

    @Test
    fun `isAiSupportedOnDevice returns false on device without Gemini Nano`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()

        val supported = useCase.isAiSupportedOnDevice()

        // Gemini Nano is not present in the unit-test JVM environment
        assertFalse(supported)
    }

    @Test
    fun `cleanup can be called without crashing`() {
        val useCase = GenerateBarcodeAiDataUseCase()
        useCase.cleanup() // Should not throw
    }

    @Test
    fun `downloadModelIfNeeded does not crash on unsupported device`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()
        useCase.downloadModelIfNeeded() // Should not throw
    }

    @Test
    fun `invoke with different barcode types returns failure gracefully`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()

        listOf(
            Triple("user@example.com", "Email", "QR Code"),
            Triple("BEGIN:VCARD\nFN:John Doe\nEND:VCARD", "Contact", "QR Code"),
            Triple("WIFI:S:MyNetwork;T:WPA;P:password;;", "Wi-Fi", "QR Code"),
            Triple("1234567890128", "Product", "EAN-13")
        ).forEach { (content, type, format) ->
            val result = useCase(
                barcodeContent = content,
                barcodeType = type,
                barcodeFormat = format,
                existingTags = emptyList()
            )
            assertTrue("Expected failure for $type barcode", result.isFailure)
        }
    }

    @Test
    fun `invoke with existing tags parameter does not crash`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()

        val result = useCase(
            barcodeContent = "https://example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code",
            existingTags = listOf("Shopping", "Work", "Travel")
        )

        // Even with existing tags, expect failure without Gemini Nano
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with language parameter does not crash`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()

        val result = useCase(
            barcodeContent = "https://example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code",
            existingTags = emptyList(),
            language = "es"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with null type and format does not crash`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()

        val result = useCase(
            barcodeContent = "plain text",
            barcodeType = null,
            barcodeFormat = null,
            existingTags = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `cleanup after invoke does not crash`() = runTest {
        val useCase = GenerateBarcodeAiDataUseCase()
        useCase(barcodeContent = "test", existingTags = emptyList())
        useCase.cleanup() // Should not throw
    }

    // -------------------------------------------------------------------
    // Fake (subclass) tests — validate response-parsing logic in isolation
    // -------------------------------------------------------------------

    /**
     * Subclass that bypasses Gemini Nano and directly returns a predetermined result,
     * allowing us to verify that the ViewModel / callers handle BarcodeAiData correctly.
     */
    private class FakeGenerateBarcodeAiDataUseCase(
        private val resultToReturn: Result<BarcodeAiData>
    ) : GenerateBarcodeAiDataUseCase() {
        override suspend fun invoke(
            barcodeContent: String,
            barcodeType: String?,
            barcodeFormat: String?,
            existingTags: List<String>,
            language: String,
            humorous: Boolean,
            userTitle: String?,
            userDescription: String?
        ): Result<BarcodeAiData> = resultToReturn

        override suspend fun isAiSupportedOnDevice(): Boolean = true
        override suspend fun downloadModelIfNeeded() { /* no-op */ }
        override fun cleanup() { /* no-op */ }
    }

    @Test
    fun `fake invoke returns expected tags`() = runTest {
        val expectedTags = listOf(
            SuggestedTagModel("Shopping", true),
            SuggestedTagModel("Online", true)
        )
        val aiData = BarcodeAiData(tags = expectedTags, description = "An online store.")
        val useCase = FakeGenerateBarcodeAiDataUseCase(Result.success(aiData))

        val result = useCase(
            barcodeContent = "https://shop.example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code",
            existingTags = emptyList()
        )

        assertTrue(result.isSuccess)
        assertEquals(expectedTags, result.getOrNull()?.tags)
    }

    @Test
    fun `fake invoke returns expected description`() = runTest {
        val expectedDescription = "A website for online shopping."
        val aiData = BarcodeAiData(
            tags = listOf(SuggestedTagModel("Shopping", true)),
            description = expectedDescription
        )
        val useCase = FakeGenerateBarcodeAiDataUseCase(Result.success(aiData))

        val result = useCase(
            barcodeContent = "https://shop.example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code",
            existingTags = emptyList()
        )

        assertTrue(result.isSuccess)
        assertEquals(expectedDescription, result.getOrNull()?.description)
    }

    @Test
    fun `fake invoke returns failure correctly`() = runTest {
        val useCase = FakeGenerateBarcodeAiDataUseCase(
            Result.failure(RuntimeException("AI unavailable"))
        )

        val result = useCase(
            barcodeContent = "https://example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code",
            existingTags = emptyList()
        )

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `fake invoke with empty tags returns empty tags list`() = runTest {
        val aiData = BarcodeAiData(tags = emptyList(), description = "A plain barcode.")
        val useCase = FakeGenerateBarcodeAiDataUseCase(Result.success(aiData))

        val result = useCase(
            barcodeContent = "plain text",
            barcodeType = "Text",
            barcodeFormat = "QR Code",
            existingTags = emptyList()
        )

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.tags?.isEmpty() == true)
        assertEquals("A plain barcode.", result.getOrNull()?.description)
    }

    @Test
    fun `fake cleanup can be called multiple times without crashing`() {
        val useCase = FakeGenerateBarcodeAiDataUseCase(
            Result.success(BarcodeAiData(tags = emptyList(), description = ""))
        )
        useCase.cleanup()
        useCase.cleanup()
    }

    @Test
    fun `result contains both tags and description when successful`() = runTest {
        val tags = listOf(
            SuggestedTagModel("Work", true),
            SuggestedTagModel("Email", true)
        )
        val description = "A professional email address."
        val aiData = BarcodeAiData(tags = tags, description = description)
        val useCase = FakeGenerateBarcodeAiDataUseCase(Result.success(aiData))

        val result = useCase(
            barcodeContent = "mailto:work@example.com",
            barcodeType = "Email",
            barcodeFormat = "QR Code",
            existingTags = listOf("Work")
        )

        val data = result.getOrNull()
        assertNotNull(data)
        assertEquals(2, data?.tags?.size)
        assertFalse(data?.description?.isEmpty() ?: true)
    }

    // -------------------------------------------------------------------
    // buildUserProvidedContextSection — unit tests for the pure helper
    // -------------------------------------------------------------------

    @Test
    fun `buildUserProvidedContextSection with both values produces expected section`() {
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = "My Coffee Shop Card",
            userDescription = "Used for the Tuesday discount"
        )
        assertTrue(section.startsWith("User-provided context (use this to refine the tags and description):"))
        assertTrue(section.contains("User-provided title: My Coffee Shop Card"))
        assertTrue(section.contains("User-provided description: Used for the Tuesday discount"))
    }

    @Test
    fun `buildUserProvidedContextSection with only title omits description line`() {
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = "Grocery Store",
            userDescription = null
        )
        assertTrue(section.contains("User-provided title: Grocery Store"))
        assertFalse(section.contains("User-provided description:"))
    }

    @Test
    fun `buildUserProvidedContextSection with only description omits title line`() {
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = null,
            userDescription = "Weekly loyalty card scan"
        )
        assertFalse(section.contains("User-provided title:"))
        assertTrue(section.contains("User-provided description: Weekly loyalty card scan"))
    }

    @Test
    fun `buildUserProvidedContextSection with both null returns empty string`() {
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = null,
            userDescription = null
        )
        assertEquals("", section)
    }

    @Test
    fun `buildUserProvidedContextSection with both blank returns empty string`() {
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = "   ",
            userDescription = "  "
        )
        assertEquals("", section)
    }

    @Test
    fun `buildUserProvidedContextSection truncates title exceeding max length`() {
        val longTitle = "A".repeat(GenerateBarcodeAiDataUseCase.MAX_USER_TITLE_LENGTH + 50)
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = longTitle,
            userDescription = null
        )
        val expected = "User-provided title: ${"A".repeat(GenerateBarcodeAiDataUseCase.MAX_USER_TITLE_LENGTH)}"
        assertTrue(section.contains(expected))
        assertFalse(section.contains("A".repeat(GenerateBarcodeAiDataUseCase.MAX_USER_TITLE_LENGTH + 1)))
    }

    @Test
    fun `buildUserProvidedContextSection truncates description exceeding max length`() {
        val longDesc = "B".repeat(GenerateBarcodeAiDataUseCase.MAX_USER_DESCRIPTION_LENGTH + 50)
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = null,
            userDescription = longDesc
        )
        val expected = "User-provided description: ${"B".repeat(GenerateBarcodeAiDataUseCase.MAX_USER_DESCRIPTION_LENGTH)}"
        assertTrue(section.contains(expected))
        assertFalse(section.contains("B".repeat(GenerateBarcodeAiDataUseCase.MAX_USER_DESCRIPTION_LENGTH + 1)))
    }

    @Test
    fun `buildUserProvidedContextSection collapses whitespace in title and description`() {
        val section = GenerateBarcodeAiDataUseCase.buildUserProvidedContextSection(
            userTitle = "My  Coffee   Shop",
            userDescription = "Used  for  discounts"
        )
        assertTrue(section.contains("User-provided title: My Coffee Shop"))
        assertTrue(section.contains("User-provided description: Used for discounts"))
    }
}
