package cat.company.qrreader.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [extractJsonObject].
 *
 * Pure JVM — no Android framework dependencies needed.
 */
class AiResponseUtilsTest {

    @Test
    fun `clean JSON is returned unchanged`() {
        val input = """{"tags": ["Work", "Finance"]}"""
        assertEquals(input, extractJsonObject(input))
    }

    @Test
    fun `preamble text before JSON is stripped`() {
        val input = """Sure, here you go: {"tags": ["Work", "Finance"]}"""
        assertEquals("""{"tags": ["Work", "Finance"]}""", extractJsonObject(input))
    }

    @Test
    fun `postamble text after JSON is stripped`() {
        val input = """{"tags": ["Work"]} I hope that helps!"""
        assertEquals("""{"tags": ["Work"]}""", extractJsonObject(input))
    }

    @Test
    fun `preamble and postamble are both stripped`() {
        val input = """Here is the result: {"description": "A QR code."} Let me know!"""
        assertEquals("""{"description": "A QR code."}""", extractJsonObject(input))
    }

    @Test
    fun `markdown json code fence is stripped`() {
        val input = "```json\n{\"tags\": [\"Work\"]}\n```"
        assertEquals("""{"tags": ["Work"]}""", extractJsonObject(input))
    }

    @Test
    fun `markdown plain code fence is stripped`() {
        val input = "```\n{\"tags\": [\"Work\"]}\n```"
        assertEquals("""{"tags": ["Work"]}""", extractJsonObject(input))
    }

    @Test
    fun `markdown code fence with preamble is handled`() {
        val input = "Here you go:\n```json\n{\"description\": \"A URL.\"}\n```\nDone."
        assertEquals("""{"description": "A URL."}""", extractJsonObject(input))
    }

    @Test
    fun `text without braces is returned as-is`() {
        val input = "Work, Finance, Travel"
        assertEquals(input, extractJsonObject(input))
    }

    @Test
    fun `empty string is returned as-is`() {
        assertEquals("", extractJsonObject(""))
    }

    @Test
    fun `nested JSON objects are preserved`() {
        val input = """{"outer": {"inner": "value"}}"""
        assertEquals(input, extractJsonObject(input))
    }
}
