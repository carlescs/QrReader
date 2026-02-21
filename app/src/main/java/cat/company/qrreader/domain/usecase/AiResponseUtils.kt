package cat.company.qrreader.domain.usecase

/**
 * Extracts the first complete JSON object from an AI response that may contain
 * markdown code fences (```json ... ```) or other surrounding text.
 *
 * Gemini Nano sometimes wraps its JSON output in code fences even when instructed
 * not to, so this helper strips those fences and locates the `{…}` payload.
 *
 * @param response The raw text returned by the AI model.
 * @return The extracted JSON object string, or `null` if none was found.
 */
internal fun extractJsonObject(response: String): String? {
    // Remove code-fence markers: ```json, ```JSON, ``` (zero or more letters after ```)
    val stripped = response
        .replace(Regex("```[a-zA-Z]*"), "")
        .trim()

    val start = stripped.indexOf('{')
    if (start == -1) return null

    // Walk forward with brace-depth tracking to find the matching closing brace,
    // correctly handling nested objects and braces inside string literals.
    var depth = 0
    var inString = false
    var escaped = false

    for (i in start until stripped.length) {
        val c = stripped[i]
        when {
            escaped -> escaped = false
            c == '\\' && inString -> escaped = true
            c == '"' -> inString = !inString
            !inString && c == '{' -> depth++
            !inString && c == '}' -> {
                depth--
                if (depth == 0) return stripped.substring(start, i + 1)
            }
        }
    }
    return null
}
