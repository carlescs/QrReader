package cat.company.qrreader.domain.usecase

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for JSON extraction from AI responses with extra text.
 *
 * These tests validate the core logic for extracting JSON from responses
 * that may contain explanatory text before or after the JSON.
 */
class JsonExtractionTest {

    /**
     * Helper function that mimics the extractJsonObject logic from the use cases
     */
    private fun extractJsonObject(text: String): String {
        val startIndex = text.indexOf('{')
        val endIndex = text.lastIndexOf('}')

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            return text
        }

        return text.substring(startIndex, endIndex + 1)
    }

    @Test
    fun `extractJsonObject handles clean JSON response`() {
        val response = """{"tags": ["Shopping", "Online", "Receipt"]}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(3, tags.length())
        assertEquals("Shopping", tags.getString(0))
        assertEquals("Online", tags.getString(1))
        assertEquals("Receipt", tags.getString(2))
    }

    @Test
    fun `extractJsonObject handles response with prefix text`() {
        val response = """Here are your tags: {"tags": ["Work", "Email"]}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(2, tags.length())
        assertEquals("Work", tags.getString(0))
        assertEquals("Email", tags.getString(1))
    }

    @Test
    fun `extractJsonObject handles response with suffix text`() {
        val response = """{"tags": ["Travel", "Flight"]} I hope this helps!"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(2, tags.length())
        assertEquals("Travel", tags.getString(0))
        assertEquals("Flight", tags.getString(1))
    }

    @Test
    fun `extractJsonObject handles response with both prefix and suffix`() {
        val response = """Here's the JSON response:
            |{"tags": ["Health", "Medical"]}
            |Let me know if you need anything else.""".trimMargin()
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(2, tags.length())
        assertEquals("Health", tags.getString(0))
        assertEquals("Medical", tags.getString(1))
    }

    @Test
    fun `extractJsonObject handles description JSON`() {
        val response = """{"description": "A QR code for accessing a restaurant menu."}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val description = json.getString("description")

        assertEquals("A QR code for accessing a restaurant menu.", description)
    }

    @Test
    fun `extractJsonObject handles description with prefix`() {
        val response = """Sure! Here's the description: {"description": "Online shopping receipt from Amazon."}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val description = json.getString("description")

        assertEquals("Online shopping receipt from Amazon.", description)
    }

    @Test
    fun `extractJsonObject handles description with newlines`() {
        val response = """
            I understand you need a description.

            {"description": "A Wi-Fi network configuration QR code."}

            This should work for you!
        """.trimIndent()
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val description = json.getString("description")

        assertEquals("A Wi-Fi network configuration QR code.", description)
    }

    @Test
    fun `extractJsonObject handles multiline JSON`() {
        val response = """
            {
              "tags": [
                "Shopping",
                "Groceries"
              ]
            }
        """.trimIndent()
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(2, tags.length())
        assertEquals("Shopping", tags.getString(0))
        assertEquals("Groceries", tags.getString(1))
    }

    @Test
    fun `extractJsonObject handles response without braces`() {
        val response = """Shopping, Online, Receipt"""
        val extracted = extractJsonObject(response)

        // Should return original text when no braces found
        assertEquals(response, extracted)
    }

    @Test
    fun `extractJsonObject handles empty response`() {
        val response = ""
        val extracted = extractJsonObject(response)

        assertEquals("", extracted)
    }

    @Test
    fun `extractJsonObject handles nested JSON objects`() {
        val response = """{"tags": ["Work"], "meta": {"confidence": 0.95}}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        assertNotNull(json.getJSONArray("tags"))
        assertNotNull(json.getJSONObject("meta"))
    }

    @Test
    fun `extractJsonObject handles JSON with escaped quotes`() {
        val response = """{"description": "A link to \"Example Site\" homepage."}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val description = json.getString("description")

        assertEquals("A link to \"Example Site\" homepage.", description)
    }

    @Test
    fun `extractJsonObject handles single tag`() {
        val response = """{"tags": ["Important"]}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(1, tags.length())
        assertEquals("Important", tags.getString(0))
    }

    @Test
    fun `extractJsonObject handles empty tag array`() {
        val response = """{"tags": []}"""
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(0, tags.length())
    }

    @Test
    fun `extractJsonObject with markdown code block`() {
        val response = """
            Here's your JSON:
            ```json
            {"tags": ["Code", "Development"]}
            ```
        """.trimIndent()
        val extracted = extractJsonObject(response)

        val json = JSONObject(extracted)
        val tags = json.getJSONArray("tags")

        assertEquals(2, tags.length())
        assertEquals("Code", tags.getString(0))
        assertEquals("Development", tags.getString(1))
    }
}
