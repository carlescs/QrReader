package cat.company.qrreader.domain.usecase

import java.net.URI

/**
 * Extracts structured context from barcode content for richer AI prompts.
 *
 * Parses the raw barcode string client-side (no network calls) and returns a
 * formatted list of facts that help Gemini Nano produce more accurate results.
 *
 * @param content The raw barcode content string.
 * @param barcodeType Human-readable type passed to the use case (e.g., "URL", "Wi-Fi").
 * @return Newline-separated bullet points, or an empty string when nothing useful was found.
 */
internal fun enrichedBarcodeContext(content: String, barcodeType: String?): String {
    val facts = mutableListOf<String>()

    when {
        // ── URLs ─────────────────────────────────────────────────────────────
        content.startsWith("http://", ignoreCase = true) ||
        content.startsWith("https://", ignoreCase = true) -> {
            try {
                val uri = URI(content)
                val host = uri.host?.removePrefix("www.")
                if (!host.isNullOrBlank()) facts += "Domain: $host"
                KNOWN_SERVICES[host?.lowercase()]?.let { facts += "Known service: $it" }
                    ?: host?.split(".")?.lastOrNull()?.let { tld ->
                        when (tld) {
                            "edu", "ac" -> facts += "Educational institution"
                            "gov" -> facts += "Government website"
                            "org" -> facts += "Non-profit or open organisation"
                        }
                    }
                uri.path?.let { path ->
                    when {
                        path.contains("/dp/") || path.contains("/product/") ->
                            facts += "Likely a product page"
                        path.contains("/invoice") || path.contains("/receipt") ->
                            facts += "Likely an invoice or receipt"
                        path.contains("/event") || path.contains("/ticket") ->
                            facts += "Likely an event or ticket"
                        path.contains("/menu") ->
                            facts += "Likely a restaurant menu"
                        path.contains("/watch") || path.contains("/video") ->
                            facts += "Likely a video page"
                    }
                }
            } catch (_: Exception) { /* URI parsing failed – skip context */ }
        }

        // ── Wi-Fi QR  WIFI:T:WPA;S:NetworkName;P:pass;; ──────────────────────
        content.startsWith("WIFI:", ignoreCase = true) -> {
            Regex("S:([^;]+)").find(content)?.groupValues?.getOrNull(1)
                ?.let { facts += "Network name (SSID): $it" }
            Regex("T:([^;]+)").find(content)?.groupValues?.getOrNull(1)
                ?.let { facts += "Security type: $it" }
        }

        // ── vCard contact ─────────────────────────────────────────────────────
        content.startsWith("BEGIN:VCARD", ignoreCase = true) -> {
            Regex("^FN:(.+)$", RegexOption.MULTILINE).find(content)?.groupValues?.getOrNull(1)?.trim()
                ?.let { facts += "Contact name: $it" }
            Regex("^ORG:(.+)$", RegexOption.MULTILINE).find(content)?.groupValues?.getOrNull(1)?.trim()
                ?.let { facts += "Organization: $it" }
            Regex("^TITLE:(.+)$", RegexOption.MULTILINE).find(content)?.groupValues?.getOrNull(1)?.trim()
                ?.let { facts += "Job title: $it" }
        }

        // ── Calendar event  BEGIN:VEVENT … ────────────────────────────────────
        content.startsWith("BEGIN:VEVENT", ignoreCase = true) -> {
            Regex("^SUMMARY:(.+)$", RegexOption.MULTILINE).find(content)?.groupValues?.getOrNull(1)?.trim()
                ?.let { facts += "Event title: $it" }
            Regex("^LOCATION:(.+)$", RegexOption.MULTILINE).find(content)?.groupValues?.getOrNull(1)?.trim()
                ?.let { facts += "Event location: $it" }
        }

        // ── mailto: ───────────────────────────────────────────────────────────
        content.startsWith("mailto:", ignoreCase = true) -> {
            content.removePrefix("mailto:").substringBefore("?").substringAfter("@")
                .takeIf { it.isNotBlank() }?.let { facts += "Email domain: $it" }
        }

        // ── tel: phone ────────────────────────────────────────────────────────
        content.startsWith("tel:", ignoreCase = true) -> {
            countryFromPhonePrefix(content.removePrefix("tel:"))
                ?.let { facts += "Country: $it" }
        }

        // ── ISBN (type hint or 978/979 EAN-13) ───────────────────────────────
        barcodeType == "ISBN" ||
        (content.length == 13 && content.all { it.isDigit() } &&
                (content.startsWith("978") || content.startsWith("979"))) -> {
            facts += "Book (ISBN-13)"
        }

        // ── Product barcode (EAN-8/13, UPC-A/E) ──────────────────────────────
        barcodeType == "Product" && content.length in 7..14 && content.all { it.isDigit() } -> {
            eanCountryHint(content)?.let { facts += "Product origin: $it" }
        }
    }

    return if (facts.isEmpty()) "" else facts.joinToString("\n") { "- $it" }
}

/** Maps lowercase hostnames to a brief service description. */
private val KNOWN_SERVICES = mapOf(
    "amazon.com"        to "Amazon (online shopping)",
    "amazon.co.uk"      to "Amazon UK (online shopping)",
    "amazon.de"         to "Amazon DE (online shopping)",
    "amazon.es"         to "Amazon ES (online shopping)",
    "amazon.fr"         to "Amazon FR (online shopping)",
    "amzn.to"           to "Amazon (short link)",
    "youtube.com"       to "YouTube (video platform)",
    "youtu.be"          to "YouTube (video platform)",
    "spotify.com"       to "Spotify (music streaming)",
    "netflix.com"       to "Netflix (video streaming)",
    "linkedin.com"      to "LinkedIn (professional network)",
    "facebook.com"      to "Facebook (social media)",
    "fb.com"            to "Facebook (social media)",
    "instagram.com"     to "Instagram (photo sharing)",
    "twitter.com"       to "Twitter/X (social media)",
    "x.com"             to "Twitter/X (social media)",
    "github.com"        to "GitHub (code hosting)",
    "paypal.com"        to "PayPal (payment)",
    "apple.com"         to "Apple",
    "apps.apple.com"    to "Apple App Store",
    "play.google.com"   to "Google Play Store",
    "maps.google.com"   to "Google Maps",
    "wa.me"             to "WhatsApp (messaging)",
    "t.me"              to "Telegram (messaging)",
    "bit.ly"            to "Shortened URL (Bitly)",
    "tinyurl.com"       to "Shortened URL (TinyURL)",
    "goo.gl"            to "Shortened URL (Google)",
    "t.co"              to "Shortened URL (Twitter/X)",
)

/** Derives a country/region name from an E.164 phone prefix. */
private fun countryFromPhonePrefix(number: String): String? {
    if (!number.startsWith("+")) return null
    return when {
        number.startsWith("+1")  -> "US/Canada"
        number.startsWith("+44") -> "UK"
        number.startsWith("+33") -> "France"
        number.startsWith("+34") -> "Spain"
        number.startsWith("+39") -> "Italy"
        number.startsWith("+49") -> "Germany"
        number.startsWith("+55") -> "Brazil"
        number.startsWith("+86") -> "China"
        number.startsWith("+81") -> "Japan"
        number.startsWith("+82") -> "South Korea"
        number.startsWith("+91") -> "India"
        number.startsWith("+7")  -> "Russia"
        number.startsWith("+61") -> "Australia"
        number.startsWith("+52") -> "Mexico"
        else -> null
    }
}

/** Derives a product-origin hint from the GS1 prefix of an EAN barcode. */
private fun eanCountryHint(code: String): String? {
    val prefix = code.take(3).toIntOrNull() ?: return null
    return when (prefix) {
        in 0..19    -> "USA/Canada"
        in 30..37   -> "France"
        in 40..44   -> "Germany"
        in 45..49   -> "Japan"
        in 50..59   -> "UK"
        in 600..601 -> "South Africa"
        in 690..699 -> "China"
        84          -> "Spain"
        in 80..83   -> "Italy"
        in 70..79   -> "Nordic countries"
        in 520..521 -> "Greece"
        560         -> "Portugal"
        590         -> "Poland"
        else        -> null
    }
}
