package cat.company.qrreader.domain.usecase

/**
 * Strips markdown code-block fences that Gemini Nano sometimes wraps around JSON responses.
 *
 * Handles patterns such as:
 * - ` ```json\n{...}\n``` `
 * - ` ```JSON\n{...}\n``` ` (case-insensitive)
 * - ` ```\n{...}\n``` `
 * - Plain `{...}` (returned unchanged)
 *
 * After stripping fences, the function looks for the first `{` and last `}` to extract
 * the JSON object, tolerating any stray text before or after the braces.
 * Using `lastIndexOf('}')` is safe for valid JSON because the outermost closing brace
 * is always the last `}` in a well-formed JSON object.
 *
 * @param response Raw text returned by the generative model.
 * @return The trimmed JSON string, or the original [response] if no JSON object is found.
 */
internal fun extractJsonFromAiResponse(response: String): String {
    // Strip markdown code-block fences, case-insensitive for the language tag (e.g. ```json, ```JSON)
    val stripped = response
        .trim()
        .replace(Regex("^```(?i:json)?\\s*"), "")
        .replace(Regex("\\s*```$"), "")
        .trim()

    // Extract the outermost JSON object { … }
    val start = stripped.indexOf('{')
    val end = stripped.lastIndexOf('}')
    return if (start != -1 && end > start) stripped.substring(start, end + 1) else stripped
}

/**
 * Converts an ISO 639-1 language code to a full English language name
 * suitable for use in AI prompts.
 */
internal fun languageNameForPrompt(code: String): String = when (code) {
    "es" -> "Spanish"
    "fr" -> "French"
    "de" -> "German"
    "it" -> "Italian"
    "pt" -> "Portuguese"
    "zh" -> "Chinese"
    "ja" -> "Japanese"
    "ko" -> "Korean"
    "ar" -> "Arabic"
    else -> "English"
}
