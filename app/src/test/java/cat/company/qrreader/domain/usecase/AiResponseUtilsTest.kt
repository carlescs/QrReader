package cat.company.qrreader.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiResponseUtilsTest {

    @Test
    fun `extractJsonObject returns JSON from plain response`() {
        val input = """{"description": "A plain product barcode."}"""
        assertEquals(input, extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject strips json code fence`() {
        val input = "```json\n{\"tags\": [\"Food\", \"Grocery\"]}\n```"
        assertEquals("{\"tags\": [\"Food\", \"Grocery\"]}", extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject strips plain code fence`() {
        val input = "```\n{\"description\": \"A barcode.\"}\n```"
        assertEquals("{\"description\": \"A barcode.\"}", extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject strips preamble text before JSON`() {
        val input = "Sure, here is the JSON:\n{\"tags\": [\"Work\"]}"
        assertEquals("{\"tags\": [\"Work\"]}", extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject strips postamble text after JSON`() {
        val input = "{\"description\": \"A product.\"}\nHope that helps!"
        assertEquals("{\"description\": \"A product.\"}", extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject handles response with preamble and code fence`() {
        val input = "Here is the result:\n```json\n{\"description\": \"Test description.\"}\n```"
        assertEquals("{\"description\": \"Test description.\"}", extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject returns null when no JSON object present`() {
        val input = "Tag1, Tag2, Tag3"
        assertNull(extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject handles trailing garbage with closing brace`() {
        val input = "{\"description\": \"A product.\"} I hope this helps! }"
        assertEquals("{\"description\": \"A product.\"}", extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject handles nested JSON objects`() {
        val input = "```json\n{\"outer\": {\"inner\": \"value\"}}\n```"
        assertEquals("{\"outer\": {\"inner\": \"value\"}}", extractJsonObject(input))
    }

    @Test
    fun `extractJsonObject handles tags array response correctly`() {
        val input = "```json\n{\"tags\": [\"Restaurant Supplies\", \"Spanish Product\"]}\n```"
        val result = extractJsonObject(input)
        assertEquals("{\"tags\": [\"Restaurant Supplies\", \"Spanish Product\"]}", result)
    }
}
