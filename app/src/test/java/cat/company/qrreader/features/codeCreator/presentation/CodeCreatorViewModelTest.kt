package cat.company.qrreader.features.codeCreator.presentation

import android.graphics.Bitmap
import cat.company.qrreader.domain.usecase.codecreator.GenerateQrCodeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for CodeCreatorViewModel
 *
 * Tests cover:
 * - Text input management
 * - QR code generation flow
 * - State management (text, bitmap, sharing)
 * - Edge cases (empty text, special characters, etc.)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class CodeCreatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CodeCreatorViewModel
    private lateinit var fakeUseCase: FakeGenerateQrCodeUseCase

    // Fake use case for testing
    private class FakeGenerateQrCodeUseCase : GenerateQrCodeUseCase() {
        var lastInput: String? = null
        var resultToReturn: Bitmap? = null
        var shouldThrowException = false

        override fun invoke(text: String): Bitmap? {
            lastInput = text
            if (shouldThrowException) {
                throw RuntimeException("Test exception")
            }
            return resultToReturn
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeUseCase = FakeGenerateQrCodeUseCase()
        viewModel = CodeCreatorViewModel(fakeUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isCorrect() {
        assertEquals("", viewModel.text.value)
        assertNull(viewModel.qrCodeBitmap.value)
        assertFalse(viewModel.isSharing.value)
    }

    @Test
    fun onTextChanged_updatesTextState() = runTest {
        val testText = "Hello World"
        
        viewModel.onTextChanged(testText)
        advanceUntilIdle()
        
        assertEquals(testText, viewModel.text.value)
        assertEquals(testText, fakeUseCase.lastInput)
    }

    @Test
    fun onTextChanged_withEmptyText_clearsQrCodeBitmap() = runTest {
        // First set a bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = bitmap
        viewModel.onTextChanged("Test")
        advanceUntilIdle()
        
        // Now clear with empty text
        fakeUseCase.resultToReturn = null
        viewModel.onTextChanged("")
        advanceUntilIdle()
        
        assertEquals("", viewModel.text.value)
        assertNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun onTextChanged_triggersQrCodeGeneration() = runTest {
        val testText = "Generate QR"
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged(testText)
        advanceUntilIdle()
        
        assertEquals(testText, fakeUseCase.lastInput)
        assertNotNull(viewModel.qrCodeBitmap.value)
        assertEquals(mockBitmap, viewModel.qrCodeBitmap.value)
    }

    @Test
    fun onTextChanged_withLongText_handlesCorrectly() = runTest {
        val longText = "This is a very long text ".repeat(50)
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged(longText)
        advanceUntilIdle()
        
        assertEquals(longText, viewModel.text.value)
        assertEquals(longText, fakeUseCase.lastInput)
        assertNotNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun onTextChanged_withSpecialCharacters_handlesCorrectly() = runTest {
        val specialText = "https://example.com?param=value&other=123#fragment"
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged(specialText)
        advanceUntilIdle()
        
        assertEquals(specialText, viewModel.text.value)
        assertEquals(specialText, fakeUseCase.lastInput)
        assertNotNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun onTextChanged_withUnicodeCharacters_handlesCorrectly() = runTest {
        val unicodeText = "Hello 世界 🌍"
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged(unicodeText)
        advanceUntilIdle()
        
        assertEquals(unicodeText, viewModel.text.value)
        assertNotNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun clearText_resetsTextAndBitmap() = runTest {
        // First set some text and bitmap
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        viewModel.onTextChanged("Test Text")
        advanceUntilIdle()
        
        // Now clear
        viewModel.clearText()
        
        assertEquals("", viewModel.text.value)
        assertNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun clearText_whenAlreadyEmpty_staysEmpty() {
        viewModel.clearText()
        
        assertEquals("", viewModel.text.value)
        assertNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun setSharing_updatesIsSharing() {
        assertFalse(viewModel.isSharing.value)
        
        viewModel.setSharing(true)
        assertTrue(viewModel.isSharing.value)
        
        viewModel.setSharing(false)
        assertFalse(viewModel.isSharing.value)
    }

    @Test
    fun setSharing_canToggleMultipleTimes() {
        viewModel.setSharing(true)
        assertTrue(viewModel.isSharing.value)
        
        viewModel.setSharing(false)
        assertFalse(viewModel.isSharing.value)
        
        viewModel.setSharing(true)
        assertTrue(viewModel.isSharing.value)
        
        viewModel.setSharing(false)
        assertFalse(viewModel.isSharing.value)
    }

    @Test
    fun multipleTextChanges_eachTriggersGeneration() = runTest {
        val texts = listOf("First", "Second", "Third")
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        texts.forEach { text ->
            viewModel.onTextChanged(text)
            advanceUntilIdle()
            assertEquals(text, viewModel.text.value)
            assertEquals(text, fakeUseCase.lastInput)
        }
    }

    @Test
    fun onTextChanged_withNullBitmapFromUseCase_handlesGracefully() = runTest {
        fakeUseCase.resultToReturn = null
        
        viewModel.onTextChanged("Test")
        advanceUntilIdle()
        
        assertEquals("Test", viewModel.text.value)
        assertNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun stateManagement_independentStates() = runTest {
        // Test that text, bitmap, and sharing states are independent
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged("Test")
        advanceUntilIdle()
        viewModel.setSharing(true)
        
        assertEquals("Test", viewModel.text.value)
        assertNotNull(viewModel.qrCodeBitmap.value)
        assertTrue(viewModel.isSharing.value)
        
        // Clear text doesn't affect sharing state
        viewModel.clearText()
        assertTrue(viewModel.isSharing.value)
        
        // Setting sharing doesn't affect text
        viewModel.onTextChanged("New")
        advanceUntilIdle()
        viewModel.setSharing(false)
        assertEquals("New", viewModel.text.value)
    }

    @Test
    fun rapidTextChanges_lastOneWins() = runTest {
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        // Rapidly change text
        viewModel.onTextChanged("First")
        viewModel.onTextChanged("Second")
        viewModel.onTextChanged("Third")
        advanceUntilIdle()
        
        // Last change should be reflected
        assertEquals("Third", viewModel.text.value)
    }

    @Test
    fun textWithWhitespace_preservesWhitespace() = runTest {
        val textWithSpaces = "  Text with   spaces  "
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged(textWithSpaces)
        advanceUntilIdle()
        
        assertEquals(textWithSpaces, viewModel.text.value)
        assertEquals(textWithSpaces, fakeUseCase.lastInput)
    }

    @Test
    fun textWithNewlines_handlesCorrectly() = runTest {
        val textWithNewlines = "Line 1\nLine 2\nLine 3"
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged(textWithNewlines)
        advanceUntilIdle()
        
        assertEquals(textWithNewlines, viewModel.text.value)
        assertNotNull(viewModel.qrCodeBitmap.value)
    }

    @Test
    fun onTextChanged_afterClear_worksCorrectly() = runTest {
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        fakeUseCase.resultToReturn = mockBitmap
        
        viewModel.onTextChanged("First")
        advanceUntilIdle()
        viewModel.clearText()
        viewModel.onTextChanged("Second")
        advanceUntilIdle()
        
        assertEquals("Second", viewModel.text.value)
        assertNotNull(viewModel.qrCodeBitmap.value)
    }
}
