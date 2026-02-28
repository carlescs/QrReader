package cat.company.qrreader.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [languageNameForPrompt].
 *
 * Verifies that all mapped ISO 639-1 codes return the correct English language name
 * and that unknown codes fall back to "English".
 */
class AiLanguageUtilsTest {

    @Test
    fun `es returns Spanish`() {
        assertEquals("Spanish", languageNameForPrompt("es"))
    }

    @Test
    fun `fr returns French`() {
        assertEquals("French", languageNameForPrompt("fr"))
    }

    @Test
    fun `de returns German`() {
        assertEquals("German", languageNameForPrompt("de"))
    }

    @Test
    fun `it returns Italian`() {
        assertEquals("Italian", languageNameForPrompt("it"))
    }

    @Test
    fun `pt returns Portuguese`() {
        assertEquals("Portuguese", languageNameForPrompt("pt"))
    }

    @Test
    fun `zh returns Chinese`() {
        assertEquals("Chinese", languageNameForPrompt("zh"))
    }

    @Test
    fun `ja returns Japanese`() {
        assertEquals("Japanese", languageNameForPrompt("ja"))
    }

    @Test
    fun `ko returns Korean`() {
        assertEquals("Korean", languageNameForPrompt("ko"))
    }

    @Test
    fun `ar returns Arabic`() {
        assertEquals("Arabic", languageNameForPrompt("ar"))
    }

    @Test
    fun `en returns English`() {
        assertEquals("English", languageNameForPrompt("en"))
    }

    @Test
    fun `unknown code returns English`() {
        assertEquals("English", languageNameForPrompt("xx"))
    }

    @Test
    fun `empty string returns English`() {
        assertEquals("English", languageNameForPrompt(""))
    }

    @Test
    fun `uppercase code returns English (case sensitive)`() {
        // The function uses exact match, so uppercase should fall through to default
        assertEquals("English", languageNameForPrompt("ES"))
    }
}

