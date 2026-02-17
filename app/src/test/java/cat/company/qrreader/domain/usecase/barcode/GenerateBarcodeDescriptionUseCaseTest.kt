package cat.company.qrreader.domain.usecase.barcode

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for GenerateBarcodeDescriptionUseCase
 */
class GenerateBarcodeDescriptionUseCaseTest {
    
    @Test
    fun `use case can be instantiated`() {
        // Test that use case can be created
        val useCase = GenerateBarcodeDescriptionUseCase()
        assertTrue(useCase != null)
    }
    
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
