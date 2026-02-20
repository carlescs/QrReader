package cat.company.qrreader.domain.usecase

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
