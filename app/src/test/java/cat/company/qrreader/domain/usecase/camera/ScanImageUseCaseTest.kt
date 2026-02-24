package cat.company.qrreader.domain.usecase.camera

import android.net.Uri
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Unit tests for ScanImageUseCase.
 *
 * Note: Full end-to-end scanning cannot be tested in unit tests because
 * ML Kit's BarcodeScanning requires Google Play Services / hardware.
 * These tests verify that the use case can be instantiated and that
 * error handling (e.g. invalid URIs) is in place.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ScanImageUseCaseTest {

    @Test
    fun `use case can be instantiated`() {
        val useCase = ScanImageUseCase()
        assertNotNull(useCase)
    }

    @Test
    fun `invoke with invalid URI propagates exception`() = runTest {
        val useCase = ScanImageUseCase()
        val context = RuntimeEnvironment.getApplication()
        val invalidUri = Uri.parse("file:///non/existent/path/image.jpg")

        try {
            useCase(context, invalidUri)
            fail("Expected an exception for an invalid URI")
        } catch (_: Exception) {
            // Expected: IOException from InputImage.fromFilePath or ML Kit failure
        }
    }
}
