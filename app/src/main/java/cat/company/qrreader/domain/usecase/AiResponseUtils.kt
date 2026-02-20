package cat.company.qrreader.domain.usecase

/**
 * Extracts a JSON object from an AI response that may contain extra text.
 *
 * Gemini Nano sometimes wraps the JSON in markdown code fences or adds
 * explanatory text before/after the JSON object. This function strips such
 * noise and returns just the JSON object substring.
 *
 * @param text Raw text from the AI model.
 * @return The extracted JSON object string, or the original [text] if no
 *         JSON object delimiters ({…}) were found.
 */
internal fun extractJsonObject(text: String): String {
    // Strip markdown code fences (```json ... ``` or ``` ... ```) as a single pass
    val stripped = text
        .replace(Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE), "$1")
        .trim()

    val start = stripped.indexOf('{')
    val end = stripped.lastIndexOf('}')

    // Note: using first '{' and last '}' correctly handles nested objects. In the
    // unlikely case that the AI returns multiple JSON objects (e.g. one followed by
    // a fragment), parsing will fail and the caller's catch block handles it.
    return if (start != -1 && end > start) {
        stripped.substring(start, end + 1)
    } else {
        stripped
    }
}
