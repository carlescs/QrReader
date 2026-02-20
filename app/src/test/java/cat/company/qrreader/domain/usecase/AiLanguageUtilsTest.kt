package cat.company.qrreader.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [extractJsonFromAiResponse].
 *
 * These tests are pure JVM — no Android framework dependencies needed.
 */
class AiLanguageUtilsTest {

    // ── Plain JSON (no fences) ────────────────────────────────────────────────

    @Test
    fun `plain JSON object is returned unchanged`() {
        val input = """{"description": "A simple product barcode."}"""
        assertEquals(input, extractJsonFromAiResponse(input))
    }

    @Test
    fun `plain JSON tags array is returned unchanged`() {
        val input = """{"tags": ["Tag1", "Tag2"]}"""
        assertEquals(input, extractJsonFromAiResponse(input))
    }

    // ── Markdown code fences ──────────────────────────────────────────────────

    @Test
    fun `backtick-json fence with newlines is stripped`() {
        val input = "```json\n{\"description\": \"Some description.\"}\n```"
        assertEquals("""{"description": "Some description."}""", extractJsonFromAiResponse(input))
    }

    @Test
    fun `backtick-JSON uppercase fence is stripped`() {
        val input = "```JSON\n{\"description\": \"Some description.\"}\n```"
        assertEquals("""{"description": "Some description."}""", extractJsonFromAiResponse(input))
    }

    @Test
    fun `backtick fence without json label is stripped`() {
        val input = "```\n{\"tags\": [\"Shopping\"]}\n```"
        assertEquals("""{"tags": ["Shopping"]}""", extractJsonFromAiResponse(input))
    }

    @Test
    fun `backtick-json fence on same line as braces is stripped`() {
        val input = "```json{\"tags\": [\"Restaurant Supplies\", \"Spanish Product\"]}```"
        assertEquals("""{"tags": ["Restaurant Supplies", "Spanish Product"]}""", extractJsonFromAiResponse(input))
    }

    // ── Stray text around JSON ────────────────────────────────────────────────

    @Test
    fun `leading and trailing whitespace is stripped`() {
        val input = "  \n{\"description\": \"Test.\"}  \n"
        assertEquals("""{"description": "Test."}""", extractJsonFromAiResponse(input))
    }

    @Test
    fun `stray text before opening brace is trimmed`() {
        val input = "Here is the JSON: {\"tags\": [\"Work\"]}"
        assertEquals("""{"tags": ["Work"]}""", extractJsonFromAiResponse(input))
    }

    @Test
    fun `stray text after closing brace is trimmed`() {
        val input = "{\"description\": \"A barcode.\"} Hope that helps!"
        assertEquals("""{"description": "A barcode."}""", extractJsonFromAiResponse(input))
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `response with no JSON braces is returned as-is after stripping fences`() {
        val input = "```json\nTag1, Tag2\n```"
        assertEquals("Tag1, Tag2", extractJsonFromAiResponse(input))
    }

    @Test
    fun `empty string returns empty string`() {
        assertEquals("", extractJsonFromAiResponse(""))
    }
}
