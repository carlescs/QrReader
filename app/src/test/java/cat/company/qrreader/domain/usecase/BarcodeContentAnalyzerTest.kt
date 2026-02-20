package cat.company.qrreader.domain.usecase

import org.junit.Assert.assertEquals
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

    // ── No context ───────────────────────────────────────────────────────────

    @Test
    fun `plain text returns empty string`() {
        val result = enrichedBarcodeContext("Hello world", "Text")
        assertEquals("", result)
    }
}
