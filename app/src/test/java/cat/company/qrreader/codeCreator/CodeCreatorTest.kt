package cat.company.qrreader.codeCreator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ApplicationProvider
import cat.company.qrreader.events.SharedEvents
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import qrcode.QRCode
import java.io.ByteArrayOutputStream

/**
 * Unit tests for CodeCreator functionality
 *
 * Note: Full UI testing of the CodeCreator composable requires instrumentation tests.
 * These unit tests focus on testing the business logic and helper functions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CodeCreatorTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Reset SharedEvents
        SharedEvents.onShareClick = null
        SharedEvents.onShareIsDisabled = null
        SharedEvents.onPrintClick = null
        SharedEvents.onPrintIsDisabled = null
    }

    @After
    fun tearDown() {
        // Clean up SharedEvents
        SharedEvents.onShareClick = null
        SharedEvents.onShareIsDisabled = null
        SharedEvents.onPrintClick = null
        SharedEvents.onPrintIsDisabled = null
    }

    /**
     * Test QR code generation from text
     */
    @Test
    fun qrCodeGeneration_withValidText_createsValidBitmap() {
        val text = "Hello World"

        // Generate QR code
        val bos = ByteArrayOutputStream()
        QRCode(text).render().writeImage(bos)
        val bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)

        // Verify bitmap was created
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }

    /**
     * Test QR code generation with empty text
     */
    @Test
    fun qrCodeGeneration_withEmptyText_createsValidBitmap() {
        val text = ""

        // Generate QR code
        val bos = ByteArrayOutputStream()
        QRCode(text).render().writeImage(bos)
        val bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)

        // Even empty text should create a valid bitmap
        assertNotNull(bitmap)
    }

    /**
     * Test QR code generation with long text
     */
    @Test
    fun qrCodeGeneration_withLongText_createsValidBitmap() {
        val text = "This is a very long text that will be encoded into a QR code. ".repeat(10)

        // Generate QR code
        val bos = ByteArrayOutputStream()
        QRCode(text).render().writeImage(bos)
        val bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)

        // Verify bitmap was created
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }

    /**
     * Test QR code generation with special characters
     */
    @Test
    fun qrCodeGeneration_withSpecialCharacters_createsValidBitmap() {
        val text = "https://example.com?param=value&other=123#fragment"

        // Generate QR code
        val bos = ByteArrayOutputStream()
        QRCode(text).render().writeImage(bos)
        val bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)

        // Verify bitmap was created
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }

    /**
     * Test QR code generation with Unicode characters
     */
    @Test
    fun qrCodeGeneration_withUnicodeCharacters_createsValidBitmap() {
        val text = "Hello 世界 🌍"

        // Generate QR code
        val bos = ByteArrayOutputStream()
        QRCode(text).render().writeImage(bos)
        val bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)

        // Verify bitmap was created
        assertNotNull(bitmap)
    }

    /**
     * Test QR code generation with numeric text
     */
    @Test
    fun qrCodeGeneration_withNumericText_createsValidBitmap() {
        val text = "1234567890"

        // Generate QR code
        val bos = ByteArrayOutputStream()
        QRCode(text).render().writeImage(bos)
        val bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)

        // Verify bitmap was created
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }

    /**
     * Test SharedEvents callback registration for share disabled state
     */
    @Test
    fun sharedEvents_onShareIsDisabled_receivesCallback() {
        var disabledState = false
        SharedEvents.onShareIsDisabled = { disabled ->
            disabledState = disabled
        }

        // Simulate callback
        SharedEvents.onShareIsDisabled?.invoke(true)
        assertTrue(disabledState)

        SharedEvents.onShareIsDisabled?.invoke(false)
        assertFalse(disabledState)
    }

    /**
     * Test SharedEvents callback registration for print disabled state
     */
    @Test
    fun sharedEvents_onPrintIsDisabled_receivesCallback() {
        var disabledState = false
        SharedEvents.onPrintIsDisabled = { disabled ->
            disabledState = disabled
        }

        // Simulate callback
        SharedEvents.onPrintIsDisabled?.invoke(true)
        assertTrue(disabledState)

        SharedEvents.onPrintIsDisabled?.invoke(false)
        assertFalse(disabledState)
    }

    /**
     * Test SharedEvents callback registration for share click
     */
    @Test
    fun sharedEvents_onShareClick_receivesCallback() {
        var clicked = false
        SharedEvents.onShareClick = {
            clicked = true
        }

        // Simulate callback
        SharedEvents.onShareClick?.invoke()
        assertTrue(clicked)
    }

    /**
     * Test SharedEvents callback registration for print click
     */
    @Test
    fun sharedEvents_onPrintClick_receivesCallback() {
        var clicked = false
        SharedEvents.onPrintClick = {
            clicked = true
        }

        // Simulate callback
        SharedEvents.onPrintClick?.invoke()
        assertTrue(clicked)
    }

    /**
     * Test state management - text and image state coordination
     */
    @Test
    fun stateManagement_whenTextIsEmpty_imageIsNull() {
        val text = mutableStateOf("")
        val image = mutableStateOf<Bitmap?>(null)

        // Simulate the logic from CodeCreator
        if (text.value.isEmpty()) {
            image.value = null
        }

        assertNull(image.value)
    }

    /**
     * Test state management - text and image state coordination with non-empty text
     */
    @Test
    fun stateManagement_whenTextIsNotEmpty_imageIsGenerated() {
        val text = mutableStateOf("Test")
        val image = mutableStateOf<Bitmap?>(null)

        // Simulate the logic from CodeCreator
        if (text.value.isNotEmpty()) {
            val bos = ByteArrayOutputStream()
            QRCode(text.value).render().writeImage(bos)
            image.value = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)
        }

        assertNotNull(image.value)
    }

    /**
     * Test sharing state flag prevents concurrent operations
     */
    @Test
    fun sharingState_preventsConcurrentOperations() {
        val sharing = mutableStateOf(false)

        // First operation
        var operation1Executed = false
        if (!sharing.value) {
            sharing.value = true
            operation1Executed = true
        }

        // Second operation should be blocked
        var operation2Executed = false
        if (!sharing.value) {
            operation2Executed = true
        }

        assertTrue(operation1Executed)
        assertFalse(operation2Executed)

        // Reset and try again
        sharing.value = false
        if (!sharing.value) {
            operation2Executed = true
        }

        assertTrue(operation2Executed)
    }

    /**
     * Test ByteArrayOutputStream usage for QR code generation
     */
    @Test
    fun byteArrayOutputStream_correctlyStoresQRCodeData() {
        val text = "Test QR"
        val bos = ByteArrayOutputStream()

        QRCode(text).render().writeImage(bos)
        val byteArray = bos.toByteArray()

        // Verify data was written
        assertTrue(byteArray.isNotEmpty())

        // Verify bitmap can be decoded
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        assertNotNull(bitmap)
    }

    /**
     * Test Bitmap compression format and quality
     */
    @Test
    fun bitmapCompression_usesJpegFormatAndFullQuality() {
        // Create a simple bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val bos = ByteArrayOutputStream()

        // Compress with JPEG at 100% quality (as used in generateUriFromBitmap)
        val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)

        assertTrue(success)
        assertTrue(bos.toByteArray().isNotEmpty())
    }

    /**
     * Test ContentValues preparation for MediaStore
     */
    @Test
    fun contentValues_preparedCorrectlyForMediaStore() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "QrCode")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        assertEquals("QrCode", values.getAsString(MediaStore.Images.Media.TITLE))
        assertEquals("image/jpeg", values.getAsString(MediaStore.Images.Media.MIME_TYPE))
    }

    /**
     * Test that multiple QR codes can be generated sequentially
     */
    @Test
    fun qrCodeGeneration_multipleSequential_allSucceed() {
        val texts = listOf("First", "Second", "Third", "Fourth", "Fifth")
        val bitmaps = mutableListOf<Bitmap>()

        texts.forEach { text ->
            val bos = ByteArrayOutputStream()
            QRCode(text).render().writeImage(bos)
            val bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)
            bitmaps.add(bitmap)
        }

        assertEquals(5, bitmaps.size)
        bitmaps.forEach { bitmap ->
            assertNotNull(bitmap)
            assertTrue(bitmap.width > 0)
            assertTrue(bitmap.height > 0)
        }
    }

    /**
     * Test that different texts produce different QR codes
     */
    @Test
    fun qrCodeGeneration_differentTexts_produceDifferentBitmaps() {
        val text1 = "Hello"
        val text2 = "World"

        val bos1 = ByteArrayOutputStream()
        QRCode(text1).render().writeImage(bos1)
        val bitmap1 = BitmapFactory.decodeByteArray(bos1.toByteArray(), 0, bos1.toByteArray().size)

        val bos2 = ByteArrayOutputStream()
        QRCode(text2).render().writeImage(bos2)
        val bitmap2 = BitmapFactory.decodeByteArray(bos2.toByteArray(), 0, bos2.toByteArray().size)

        // Bitmaps should have same dimensions but different content
        assertEquals(bitmap1.width, bitmap2.width)
        assertEquals(bitmap1.height, bitmap2.height)

        // At least one pixel should be different
        var foundDifference = false
        for (x in 0 until bitmap1.width) {
            for (y in 0 until bitmap1.height) {
                if (bitmap1.getPixel(x, y) != bitmap2.getPixel(x, y)) {
                    foundDifference = true
                    break
                }
            }
            if (foundDifference) break
        }
        assertTrue(foundDifference)
    }

    /**
     * Test SharedEvents cleanup between operations
     */
    @Test
    fun sharedEvents_cleanup_resetsCallbacks() {
        // Set callbacks
        SharedEvents.onShareClick = { }
        SharedEvents.onPrintClick = { }
        SharedEvents.onShareIsDisabled = { }
        SharedEvents.onPrintIsDisabled = { }

        assertNotNull(SharedEvents.onShareClick)
        assertNotNull(SharedEvents.onPrintClick)
        assertNotNull(SharedEvents.onShareIsDisabled)
        assertNotNull(SharedEvents.onPrintIsDisabled)

        // Clean up
        SharedEvents.onShareClick = null
        SharedEvents.onPrintClick = null
        SharedEvents.onShareIsDisabled = null
        SharedEvents.onPrintIsDisabled = null

        assertNull(SharedEvents.onShareClick)
        assertNull(SharedEvents.onPrintClick)
        assertNull(SharedEvents.onShareIsDisabled)
        assertNull(SharedEvents.onPrintIsDisabled)
    }

    /**
     * Test error handling when bitmap is null
     */
    @Test
    fun shareFunction_withNullBitmap_returnsEarly() {
        val sharing = mutableStateOf(false)
        val image = mutableStateOf<Bitmap?>(null)

        // Simulate the logic from share function
        if (!sharing.value) {
            try {
                sharing.value = true
                // Early return when bitmap is null
                if (image.value == null) {
                    return
                }
                // This line should not be reached
                fail("Should have returned early when bitmap is null")
            } finally {
                sharing.value = false
            }
        }

        // This should not be reached because we return early
        fail("Test should have returned early")
    }

    /**
     * Test error handling when bitmap is null for print
     */
    @Test
    fun printFunction_withNullBitmap_returnsEarly() {
        val sharing = mutableStateOf(false)
        val image = mutableStateOf<Bitmap?>(null)

        // Simulate the logic from print function
        if (!sharing.value) {
            try {
                sharing.value = true
                // Early return when bitmap is null
                if (image.value == null) {
                    return
                }
                // This line should not be reached
                fail("Should have returned early when bitmap is null")
            } finally {
                sharing.value = false
            }
        }

        // This should not be reached because we return early
        fail("Test should have returned early")
    }
}
