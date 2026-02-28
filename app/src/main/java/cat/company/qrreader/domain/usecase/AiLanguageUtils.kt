package cat.company.qrreader.domain.usecase

import java.util.Locale

/**
 * Converts an ISO 639-1 language code to a full English language name
 * suitable for use in AI prompts.
 *
 * The special value `"device"` resolves to the current device locale language.
 */
internal fun languageNameForPrompt(code: String): String {
    val resolvedCode = if (code == "device") Locale.getDefault().language else code
    return when (resolvedCode) {
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
}
