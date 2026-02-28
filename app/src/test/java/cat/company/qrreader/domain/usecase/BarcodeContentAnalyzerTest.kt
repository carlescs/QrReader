package cat.company.qrreader.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [enrichedBarcodeContext].
 *
 * These tests are pure JVM — no Android framework dependencies needed.
 */
class BarcodeContentAnalyzerTest {

    // ── URL ──────────────────────────────────────────────────────────────────

    @Test
    fun `url - extracts domain`() {
        val result = enrichedBarcodeContext("https://www.example.com/path", "URL")
        assertTrue("expected domain fact", result.contains("Domain: example.com"))
    }

    @Test
    fun `url - identifies known service`() {
        val result = enrichedBarcodeContext("https://www.youtube.com/watch?v=abc", "URL")
        assertTrue(result.contains("YouTube"))
    }

    @Test
    fun `url - detects product page path`() {
        val result = enrichedBarcodeContext("https://www.amazon.com/dp/B08XYZ123", "URL")
        assertTrue(result.contains("product page"))
    }

    @Test
    fun `url - detects edu tld`() {
        val result = enrichedBarcodeContext("https://cs.mit.edu/courses", "URL")
        assertTrue(result.contains("Educational"))
    }

    @Test
    fun `url - returns empty for unknown plain url`() {
        val result = enrichedBarcodeContext("https://myrandomsite.io/foo", "URL")
        assertTrue("domain should still be present", result.contains("Domain: myrandomsite.io"))
    }

    // ── Wi-Fi ────────────────────────────────────────────────────────────────

    @Test
    fun `wifi - extracts ssid and security`() {
        val result = enrichedBarcodeContext("WIFI:T:WPA2;S:HomeNetwork;P:secret;;", "Wi-Fi")
        assertTrue(result.contains("HomeNetwork"))
        assertTrue(result.contains("WPA2"))
    }

    // ── vCard ────────────────────────────────────────────────────────────────

    @Test
    fun `vcard - extracts name and org`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:Jane Doe\nORG:Acme Corp\nEND:VCARD"
        val result = enrichedBarcodeContext(vcard, "Contact")
        assertTrue(result.contains("Jane Doe"))
        assertTrue(result.contains("Acme Corp"))
    }

    // ── Calendar ─────────────────────────────────────────────────────────────

    @Test
    fun `calendar - extracts summary and location`() {
        val vevent = "BEGIN:VEVENT\nSUMMARY:Team Meeting\nLOCATION:Office\nEND:VEVENT"
        val result = enrichedBarcodeContext(vevent, "Calendar")
        assertTrue(result.contains("Team Meeting"))
        assertTrue(result.contains("Office"))
    }

    // ── mailto ───────────────────────────────────────────────────────────────

    @Test
    fun `mailto - extracts email domain`() {
        val result = enrichedBarcodeContext("mailto:user@company.com", "Email")
        assertTrue(result.contains("company.com"))
    }

    // ── Phone ────────────────────────────────────────────────────────────────

    @Test
    fun `phone - detects country from e164 prefix`() {
        val result = enrichedBarcodeContext("tel:+34912345678", "Phone")
        assertTrue(result.contains("Spain"))
    }

    @Test
    fun `phone - no country for unknown prefix`() {
        val result = enrichedBarcodeContext("tel:+9991234567", "Phone")
        // Unknown prefix → no country fact, but result should be empty (not crash)
        assertTrue(result.isEmpty())
    }

    // ── Product / EAN ────────────────────────────────────────────────────────

    @Test
    fun `product ean13 - detects usa origin from prefix 0xx`() {
        val result = enrichedBarcodeContext("0123456789012", "Product")
        assertTrue(result.contains("USA"))
    }

    @Test
    fun `product ean13 - detects spain origin from prefix 84x`() {
        val result = enrichedBarcodeContext("8412345678901", "Product")
        assertTrue(result.contains("Spain"))
    }

    // ── ISBN ─────────────────────────────────────────────────────────────────

    @Test
    fun `isbn - detected from type hint`() {
        val result = enrichedBarcodeContext("9780743273565", "ISBN")
        assertTrue(result.contains("ISBN"))
    }

    @Test
    fun `isbn - detected from 978 prefix even without type hint`() {
        val result = enrichedBarcodeContext("9780743273565", null)
        assertTrue(result.contains("ISBN"))
    }

    // ── URL additional paths & services ──────────────────────────────────────

    @Test
    fun `url - detects gov tld`() {
        val result = enrichedBarcodeContext("https://data.gov/datasets", "URL")
        assertTrue(result.contains("Government"))
    }

    @Test
    fun `url - detects org tld`() {
        val result = enrichedBarcodeContext("https://wikipedia.org/wiki/Test", "URL")
        assertTrue(result.contains("Non-profit") || result.contains("organisation"))
    }

    @Test
    fun `url - detects invoice path`() {
        val result = enrichedBarcodeContext("https://shop.example.com/invoice/12345", "URL")
        assertTrue(result.contains("invoice") || result.contains("receipt"))
    }

    @Test
    fun `url - detects event path`() {
        val result = enrichedBarcodeContext("https://tickets.example.com/event/fest", "URL")
        assertTrue(result.contains("event") || result.contains("ticket"))
    }

    @Test
    fun `url - detects menu path`() {
        val result = enrichedBarcodeContext("https://restaurant.example.com/menu", "URL")
        assertTrue(result.contains("menu"))
    }

    @Test
    fun `url - detects video path`() {
        val result = enrichedBarcodeContext("https://example.com/video/abc123", "URL")
        assertTrue(result.contains("video"))
    }

    @Test
    fun `url - detects ticket path`() {
        val result = enrichedBarcodeContext("https://concerts.example.com/ticket/99", "URL")
        assertTrue(result.contains("event") || result.contains("ticket"))
    }

    @Test
    fun `url - known service spotify`() {
        val result = enrichedBarcodeContext("https://spotify.com/track/abc", "URL")
        assertTrue(result.contains("Spotify"))
    }

    @Test
    fun `url - known service github`() {
        val result = enrichedBarcodeContext("https://github.com/user/repo", "URL")
        assertTrue(result.contains("GitHub"))
    }

    @Test
    fun `url - known service whatsapp`() {
        val result = enrichedBarcodeContext("https://wa.me/1234567890", "URL")
        assertTrue(result.contains("WhatsApp"))
    }

    @Test
    fun `url - known service bitly`() {
        val result = enrichedBarcodeContext("https://bit.ly/abc123", "URL")
        assertTrue(result.contains("Bitly"))
    }

    @Test
    fun `url - receipt path`() {
        val result = enrichedBarcodeContext("https://shop.example.com/receipt/r123", "URL")
        assertTrue(result.contains("invoice") || result.contains("receipt"))
    }

    @Test
    fun `url - watch path for video`() {
        val result = enrichedBarcodeContext("https://example.com/watch/v123", "URL")
        assertTrue(result.contains("video"))
    }

    // ── vCard with title ─────────────────────────────────────────────────────

    @Test
    fun `vcard - extracts job title`() {
        val vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:John\nTITLE:Engineer\nEND:VCARD"
        val result = enrichedBarcodeContext(vcard, "Contact")
        assertTrue(result.contains("Engineer"))
    }

    // ── Phone additional countries ───────────────────────────────────────────

    @Test
    fun `phone - detects UK`() {
        val result = enrichedBarcodeContext("tel:+442012345678", "Phone")
        assertTrue(result.contains("UK"))
    }

    @Test
    fun `phone - detects France`() {
        val result = enrichedBarcodeContext("tel:+33123456789", "Phone")
        assertTrue(result.contains("France"))
    }

    @Test
    fun `phone - detects Germany`() {
        val result = enrichedBarcodeContext("tel:+4930123456", "Phone")
        assertTrue(result.contains("Germany"))
    }

    @Test
    fun `phone - detects Italy`() {
        val result = enrichedBarcodeContext("tel:+39061234567", "Phone")
        assertTrue(result.contains("Italy"))
    }

    @Test
    fun `phone - detects Brazil`() {
        val result = enrichedBarcodeContext("tel:+5511987654321", "Phone")
        assertTrue(result.contains("Brazil"))
    }

    @Test
    fun `phone - detects China`() {
        val result = enrichedBarcodeContext("tel:+8613912345678", "Phone")
        assertTrue(result.contains("China"))
    }

    @Test
    fun `phone - detects Japan`() {
        val result = enrichedBarcodeContext("tel:+81312345678", "Phone")
        assertTrue(result.contains("Japan"))
    }

    @Test
    fun `phone - detects South Korea`() {
        val result = enrichedBarcodeContext("tel:+82212345678", "Phone")
        assertTrue(result.contains("South Korea"))
    }

    @Test
    fun `phone - detects India`() {
        val result = enrichedBarcodeContext("tel:+911234567890", "Phone")
        assertTrue(result.contains("India"))
    }

    @Test
    fun `phone - detects US or Canada`() {
        val result = enrichedBarcodeContext("tel:+12125551234", "Phone")
        assertTrue(result.contains("US/Canada"))
    }

    @Test
    fun `phone - detects Russia`() {
        val result = enrichedBarcodeContext("tel:+74951234567", "Phone")
        assertTrue(result.contains("Russia"))
    }

    @Test
    fun `phone - detects Australia`() {
        val result = enrichedBarcodeContext("tel:+61291234567", "Phone")
        assertTrue(result.contains("Australia"))
    }

    @Test
    fun `phone - detects Mexico`() {
        val result = enrichedBarcodeContext("tel:+525512345678", "Phone")
        assertTrue(result.contains("Mexico"))
    }

    @Test
    fun `phone - no prefix local number returns empty`() {
        val result = enrichedBarcodeContext("tel:912345678", "Phone")
        assertTrue(result.isEmpty())
    }

    // ── EAN country hints ────────────────────────────────────────────────────

    @Test
    fun `product ean13 - detects france from prefix 030`() {
        // eanCountryHint range is 30..37, prefix "030" → 30
        val result = enrichedBarcodeContext("0301234567890", "Product")
        assertTrue(result.contains("France"))
    }

    @Test
    fun `product ean13 - detects germany from prefix 040`() {
        // eanCountryHint range is 40..44, prefix "040" → 40
        val result = enrichedBarcodeContext("0401234567890", "Product")
        assertTrue(result.contains("Germany"))
    }

    @Test
    fun `product ean13 - detects japan from prefix 049`() {
        // eanCountryHint range is 45..49, prefix "049" → 49
        val result = enrichedBarcodeContext("0491234567890", "Product")
        assertTrue(result.contains("Japan"))
    }

    @Test
    fun `product ean13 - detects uk from prefix 050`() {
        // eanCountryHint range is 50..59, prefix "050" → 50
        val result = enrichedBarcodeContext("0501234567890", "Product")
        assertTrue(result.contains("UK"))
    }

    @Test
    fun `product ean13 - detects china from prefix 69x`() {
        val result = enrichedBarcodeContext("6901234567890", "Product")
        assertTrue(result.contains("China"))
    }

    @Test
    fun `product ean13 - detects italy from prefix 80x`() {
        val result = enrichedBarcodeContext("8001234567890", "Product")
        assertTrue(result.contains("Italy"))
    }

    @Test
    fun `product ean13 - detects nordic from prefix 70x`() {
        val result = enrichedBarcodeContext("7012345678901", "Product")
        assertTrue(result.contains("Nordic"))
    }

    @Test
    fun `product ean13 - detects portugal from prefix 560`() {
        val result = enrichedBarcodeContext("5601234567890", "Product")
        assertTrue(result.contains("Portugal"))
    }

    @Test
    fun `product ean13 - detects poland from prefix 590`() {
        val result = enrichedBarcodeContext("5901234567890", "Product")
        assertTrue(result.contains("Poland"))
    }

    @Test
    fun `product ean13 - detects greece from prefix 520`() {
        val result = enrichedBarcodeContext("5201234567890", "Product")
        assertTrue(result.contains("Greece"))
    }

    @Test
    fun `product ean8 - short code handled`() {
        val result = enrichedBarcodeContext("01234567", "Product")
        assertTrue(result.contains("USA"))
    }

    @Test
    fun `product ean13 - unknown prefix returns no origin`() {
        val result = enrichedBarcodeContext("9991234567890", "Product")
        assertFalse(result.contains("origin"))
    }

    // ── ISBN with 979 prefix ─────────────────────────────────────────────────

    @Test
    fun `isbn - detected from 979 prefix without type hint`() {
        val result = enrichedBarcodeContext("9791234567890", null)
        assertTrue(result.contains("ISBN"))
    }

    // ── Calendar without location ────────────────────────────────────────────

    @Test
    fun `calendar - extracts only summary when no location`() {
        val vevent = "BEGIN:VEVENT\nSUMMARY:Standup\nEND:VEVENT"
        val result = enrichedBarcodeContext(vevent, "Calendar")
        assertTrue(result.contains("Standup"))
        assertFalse(result.contains("location"))
    }

    // ── Mailto with query parameters ─────────────────────────────────────────

    @Test
    fun `mailto - extracts domain ignoring query params`() {
        val result = enrichedBarcodeContext("mailto:user@bigcorp.com?subject=Hi", "Email")
        assertTrue(result.contains("bigcorp.com"))
    }

    // ── No context / edge cases ──────────────────────────────────────────────

    @Test
    fun `plain text returns empty string`() {
        val result = enrichedBarcodeContext("Hello world", "Text")
        assertEquals("", result)
    }

    @Test
    fun `empty string returns empty`() {
        val result = enrichedBarcodeContext("", "Text")
        assertEquals("", result)
    }

    @Test
    fun `numeric non-product content returns empty`() {
        val result = enrichedBarcodeContext("12345", "Text")
        assertEquals("", result)
    }
}
