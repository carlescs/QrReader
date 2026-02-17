package cat.company.qrreader.domain.usecase.barcode

import kotlinx.coroutines.test.runTest
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
}
