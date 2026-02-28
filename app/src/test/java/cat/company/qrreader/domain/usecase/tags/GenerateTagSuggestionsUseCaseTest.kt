package cat.company.qrreader.domain.usecase.tags

import cat.company.qrreader.domain.model.SuggestedTagModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [GenerateTagSuggestionsUseCase].
 *
 * Gemini Nano is unavailable in test environments, so these tests cover
 * error paths, safe operation, and fake-based logic.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GenerateTagSuggestionsUseCaseTest {

    @Test
    fun `invoke returns failure without Gemini Nano`() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val result = useCase(
            barcodeContent = "https://example.com",
            barcodeType = "URL",
            barcodeFormat = "QR Code",
            existingTags = emptyList()
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with existing tags fails on unsupported device`() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val result = useCase(
            barcodeContent = "WIFI:T:WPA;S:Net;P:x;;",
            barcodeType = "Wi-Fi",
            barcodeFormat = null,
            existingTags = listOf("Home", "Network")
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with custom language fails on unsupported device`() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val result = useCase(
            barcodeContent = "https://amazon.es/dp/B123",
            existingTags = emptyList(),
            language = "es"
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with null type and format fails on unsupported device`() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        val result = useCase(
            barcodeContent = "plain text",
            barcodeType = null,
            barcodeFormat = null,
            existingTags = emptyList()
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `cleanup does not crash`() {
        val useCase = GenerateTagSuggestionsUseCase()
        useCase.cleanup()
    }

    @Test
    fun `cleanup called twice does not crash`() {
        val useCase = GenerateTagSuggestionsUseCase()
        useCase.cleanup()
        useCase.cleanup()
    }

    @Test
    fun `downloadModelIfNeeded does not crash on unsupported device`() = runTest {
        val useCase = GenerateTagSuggestionsUseCase()
        useCase.downloadModelIfNeeded()
    }

    // ── Fake-based tests ─────────────────────────────────────────────────────

    @Test
    fun `fake use case returns success with predetermined tags`() = runTest {
        val fakeTags = listOf(
            SuggestedTagModel(name = "Shopping", isSelected = false),
            SuggestedTagModel(name = "Amazon", isSelected = false)
        )
        val useCase = object : GenerateTagSuggestionsUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String
            ): Result<List<SuggestedTagModel>> = Result.success(fakeTags)

            override suspend fun downloadModelIfNeeded() { /* no-op */ }
            override fun cleanup() { /* no-op */ }
        }

        val result = useCase(
            barcodeContent = "https://amazon.com/dp/B123",
            existingTags = emptyList()
        )

        assertTrue(result.isSuccess)
        val tags = result.getOrNull()!!
        assertTrue(tags.size == 2)
        assertTrue(tags[0].name == "Shopping")
        assertTrue(tags[1].name == "Amazon")
    }

    @Test
    fun `fake use case returns empty list`() = runTest {
        val useCase = object : GenerateTagSuggestionsUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String
            ): Result<List<SuggestedTagModel>> = Result.success(emptyList())

            override suspend fun downloadModelIfNeeded() { /* no-op */ }
            override fun cleanup() { /* no-op */ }
        }

        val result = useCase(barcodeContent = "test", existingTags = emptyList())

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `fake use case returns failure`() = runTest {
        val useCase = object : GenerateTagSuggestionsUseCase() {
            override suspend fun invoke(
                barcodeContent: String,
                barcodeType: String?,
                barcodeFormat: String?,
                existingTags: List<String>,
                language: String
            ): Result<List<SuggestedTagModel>> = Result.failure(
                UnsupportedOperationException("AI not available")
            )

            override suspend fun downloadModelIfNeeded() { /* no-op */ }
            override fun cleanup() { /* no-op */ }
        }

        val result = useCase(barcodeContent = "test", existingTags = emptyList())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnsupportedOperationException)
    }
}

