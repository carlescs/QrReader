package cat.company.qrreader.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for formatWifiQrText utility function
 */
class BarcodeUtilsFormatWifiTest {

    @Test
    fun formatWifiQrText_wpaWithPassword_producesCorrectFormat() {
        val result = formatWifiQrText("MyNetwork", "secret123", "WPA")
        assertEquals("WIFI:T:WPA;S:MyNetwork;P:secret123;;", result)
    }

    @Test
    fun formatWifiQrText_wepWithPassword_producesCorrectFormat() {
        val result = formatWifiQrText("OldNet", "wepkey", "WEP")
        assertEquals("WIFI:T:WEP;S:OldNet;P:wepkey;;", result)
    }

    @Test
    fun formatWifiQrText_openNetwork_omitsPassword() {
        val result = formatWifiQrText("OpenWifi", null, "nopass")
        assertEquals("WIFI:T:nopass;S:OpenWifi;;", result)
    }

    @Test
    fun formatWifiQrText_openNetworkWithPassword_ignoresPassword() {
        val result = formatWifiQrText("OpenWifi", "ignored", "nopass")
        assertEquals("WIFI:T:nopass;S:OpenWifi;;", result)
    }

    @Test
    fun formatWifiQrText_emptyPassword_omitsPasswordField() {
        val result = formatWifiQrText("Network", "", "WPA")
        assertEquals("WIFI:T:WPA;S:Network;;", result)
    }

    @Test
    fun formatWifiQrText_ssidWithSpaces_preservesSpaces() {
        val result = formatWifiQrText("My Home Network", "pass", "WPA")
        assertEquals("WIFI:T:WPA;S:My Home Network;P:pass;;", result)
    }

    @Test
    fun formatWifiQrText_ssidWithSemicolon_escapesCharacter() {
        val result = formatWifiQrText("Net;work", "pass", "WPA")
        assertEquals("WIFI:T:WPA;S:Net\\;work;P:pass;;", result)
    }

    @Test
    fun formatWifiQrText_passwordWithSpecialChars_escapesCharacters() {
        val result = formatWifiQrText("Network", "p@ss;word\\key", "WPA")
        assertEquals("WIFI:T:WPA;S:Network;P:p@ss\\;word\\\\key;;", result)
    }

    @Test
    fun formatWifiQrText_outputIsRoundTrippable() {
        val ssid = "TestNet"
        val password = "testpass"
        val securityType = "WPA"
        val qrText = formatWifiQrText(ssid, password, securityType)
        val parsed = parseWifiContent(qrText)
        assertEquals(ssid, parsed.ssid)
        assertEquals(password, parsed.password)
        assertEquals(securityType, parsed.securityType)
    }

    @Test
    fun formatWifiQrText_roundTrip_ssidWithSemicolon() {
        val ssid = "Net;work"
        val password = "pass"
        val qrText = formatWifiQrText(ssid, password, "WPA")
        val parsed = parseWifiContent(qrText)
        assertEquals(ssid, parsed.ssid)
        assertEquals(password, parsed.password)
    }

    @Test
    fun formatWifiQrText_roundTrip_passwordWithSpecialChars() {
        val ssid = "MyNet"
        val password = "p@ss;word\\key:val\"end"
        val qrText = formatWifiQrText(ssid, password, "WPA")
        val parsed = parseWifiContent(qrText)
        assertEquals(ssid, parsed.ssid)
        assertEquals(password, parsed.password)
    }

    @Test
    fun formatWifiQrText_roundTrip_ssidAndPasswordBothEscaped() {
        val ssid = "Net;name:x"
        val password = "p\\ss;w"
        val qrText = formatWifiQrText(ssid, password, "WEP")
        val parsed = parseWifiContent(qrText)
        assertEquals(ssid, parsed.ssid)
        assertEquals(password, parsed.password)
        assertEquals("WEP", parsed.securityType)
    }
}

/**
 * Unit tests for parseWifiContent utility function
 */
class BarcodeUtilsTest {

    @Test
    fun parseWifiContent_wpaWithPassword_parsesAllFields() {
        val content = "WIFI:T:WPA;S:MyNetwork;P:secret123;;"
        val result = parseWifiContent(content)
        assertEquals("MyNetwork", result.ssid)
        assertEquals("secret123", result.password)
        assertEquals("WPA", result.securityType)
    }

    @Test
    fun parseWifiContent_openNetwork_parsesNoPassword() {
        val content = "WIFI:T:nopass;S:OpenWifi;P:;;"
        val result = parseWifiContent(content)
        assertEquals("OpenWifi", result.ssid)
        assertNull(result.password)
        assertEquals("nopass", result.securityType)
    }

    @Test
    fun parseWifiContent_wepNetwork_parsesAllFields() {
        val content = "WIFI:T:WEP;S:OldNet;P:wepkey;;"
        val result = parseWifiContent(content)
        assertEquals("OldNet", result.ssid)
        assertEquals("wepkey", result.password)
        assertEquals("WEP", result.securityType)
    }

    @Test
    fun parseWifiContent_missingPassword_returnsNullPassword() {
        val content = "WIFI:T:WPA;S:Network;;"
        val result = parseWifiContent(content)
        assertEquals("Network", result.ssid)
        assertNull(result.password)
        assertEquals("WPA", result.securityType)
    }

    @Test
    fun parseWifiContent_missingSsid_returnsNullSsid() {
        val content = "WIFI:T:WPA;P:pass;;"
        val result = parseWifiContent(content)
        assertNull(result.ssid)
        assertEquals("pass", result.password)
        assertEquals("WPA", result.securityType)
    }

    @Test
    fun parseWifiContent_ssidWithSpaces_parsesCorrectly() {
        val content = "WIFI:T:WPA;S:My Home Network;P:pass;;"
        val result = parseWifiContent(content)
        assertEquals("My Home Network", result.ssid)
    }

    @Test
    fun parseWifiContent_emptyContent_returnsAllNull() {
        val result = parseWifiContent("")
        assertNull(result.ssid)
        assertNull(result.password)
        assertNull(result.securityType)
    }
}

/**
 * Unit tests for parseContactVCard utility function
 */
class ParseContactVCardTest {

    @Test
    fun parseContactVCard_standardVCard_parsesAllFields() {
        val content = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John Doe
            TEL:+1234567890
            EMAIL:john@example.com
            ORG:Example Corp
            END:VCARD
        """.trimIndent()
        val result = parseContactVCard(content)
        assertEquals("John Doe", result.name)
        assertEquals("+1234567890", result.phone)
        assertEquals("john@example.com", result.email)
        assertEquals("Example Corp", result.organization)
    }

    @Test
    fun parseContactVCard_parametrizedKeys_parsesFields() {
        val content = """
            BEGIN:VCARD
            VERSION:2.1
            FN;CHARSET=UTF-8:Jane Smith
            TEL;TYPE=CELL:+9876543210
            EMAIL;TYPE=WORK:jane@work.com
            ORG;TYPE=work:Acme Inc
            END:VCARD
        """.trimIndent()
        val result = parseContactVCard(content)
        assertEquals("Jane Smith", result.name)
        assertEquals("+9876543210", result.phone)
        assertEquals("jane@work.com", result.email)
        assertEquals("Acme Inc", result.organization)
    }

    @Test
    fun parseContactVCard_fnTakesPrecedenceOverN() {
        val content = """
            BEGIN:VCARD
            N:Doe;John;;;
            FN:John Doe
            END:VCARD
        """.trimIndent()
        val result = parseContactVCard(content)
        assertEquals("John Doe", result.name)
    }

    @Test
    fun parseContactVCard_nFieldFallback_constructsName() {
        val content = """
            BEGIN:VCARD
            N:Doe;John;;;
            END:VCARD
        """.trimIndent()
        val result = parseContactVCard(content)
        assertEquals("John Doe", result.name)
    }

    @Test
    fun parseContactVCard_foldedLines_unfoldsCorrectly() {
        val content = "BEGIN:VCARD\r\nFN:Jo\r\n hn Doe\r\nEND:VCARD"
        val result = parseContactVCard(content)
        assertEquals("John Doe", result.name)
    }

    @Test
    fun parseContactVCard_missingFields_returnsNull() {
        val content = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Only Name
            END:VCARD
        """.trimIndent()
        val result = parseContactVCard(content)
        assertEquals("Only Name", result.name)
        assertNull(result.phone)
        assertNull(result.email)
        assertNull(result.organization)
    }

    @Test
    fun parseContactVCard_emptyContent_returnsAllNull() {
        val result = parseContactVCard("")
        assertNull(result.name)
        assertNull(result.phone)
        assertNull(result.email)
        assertNull(result.organization)
    }

    @Test
    fun parseContactVCard_mecardFormat_parsesAllFields() {
        val content = "MECARD:N:John Doe;TEL:123456789;EMAIL:john@example.com;ORG:Example Corp;;"
        val result = parseContactVCard(content)
        assertEquals("John Doe", result.name)
        assertEquals("123456789", result.phone)
        assertEquals("john@example.com", result.email)
        assertEquals("Example Corp", result.organization)
    }

    @Test
    fun parseContactVCard_mecardWithoutOptionalFields_returnsNulls() {
        val content = "MECARD:N:Jane Smith;TEL:987654321;;"
        val result = parseContactVCard(content)
        assertEquals("Jane Smith", result.name)
        assertEquals("987654321", result.phone)
        assertNull(result.email)
        assertNull(result.organization)
    }

    @Test
    fun parseContactVCard_mecardCaseInsensitive_parsesCorrectly() {
        val content = "mecard:N:Bob;TEL:111;;"
        val result = parseContactVCard(content)
        assertEquals("Bob", result.name)
        assertEquals("111", result.phone)
    }
}

/**
 * Unit tests for formatContactQrText utility function, escaping and round-tripping.
 */
class BarcodeUtilsFormatContactTest {

    @Test
    fun formatContactQrText_allFields_producesCorrectVCard() {
        val result = formatContactQrText("John Doe", "+34600000000", "john@example.com", "ACME")
        assert(result.contains("BEGIN:VCARD"))
        assert(result.contains("VERSION:3.0"))
        assert(result.contains("FN:John Doe"))
        assert(result.contains("N:Doe;John;;;"))
        assert(result.contains("TEL:+34600000000"))
        assert(result.contains("EMAIL:john@example.com"))
        assert(result.contains("ORG:ACME"))
        assert(result.contains("END:VCARD"))
    }

    @Test
    fun formatContactQrText_onlyName_omitsOptionalFields() {
        val result = formatContactQrText("Alice", null, null, null)
        assert(result.contains("FN:Alice"))
        assert(result.contains("N:Alice;;;;"))
        assert(!result.contains("TEL:"))
        assert(!result.contains("EMAIL:"))
        assert(!result.contains("ORG:"))
    }

    @Test
    fun formatContactQrText_nameWithSemicolon_escapesSemicolon() {
        val result = formatContactQrText("O;Brien", null, null, null)
        assert(result.contains("FN:O\\;Brien"))
    }

    @Test
    fun formatContactQrText_nameWithBackslash_escapesBackslash() {
        val result = formatContactQrText("Back\\slash", null, null, null)
        assert(result.contains("FN:Back\\\\slash"))
    }

    @Test
    fun formatContactQrText_nameWithNewline_escapesNewline() {
        val result = formatContactQrText("Line\nBreak", null, null, null)
        assert(result.contains("FN:Line\\nBreak"))
    }

    @Test
    fun formatContactQrText_nameWithCrLf_normalizesAndEscapes() {
        val result = formatContactQrText("Line\r\nBreak", null, null, null)
        assert(result.contains("FN:Line\\nBreak"))
    }

    @Test
    fun formatContactQrText_nameWithCr_normalizesAndEscapes() {
        val result = formatContactQrText("Line\rBreak", null, null, null)
        assert(result.contains("FN:Line\\nBreak"))
    }

    @Test
    fun formatContactQrText_nFieldSingleWordName_producesCorrectN() {
        val result = formatContactQrText("Madonna", null, null, null)
        assert(result.contains("N:Madonna;;;;"))
    }

    @Test
    fun formatContactQrText_nFieldMultiWordName_splitsLastNameFirst() {
        val result = formatContactQrText("John Michael Doe", null, null, null)
        assert(result.contains("N:Doe;John Michael;;;"))
    }

    @Test
    fun formatContactQrText_roundTrip_allFields() {
        val name = "John Doe"
        val phone = "+34600000000"
        val email = "john@example.com"
        val org = "ACME"
        val qrText = formatContactQrText(name, phone, email, org)
        val parsed = parseContactVCard(qrText)
        assertEquals(name, parsed.name)
        assertEquals(phone, parsed.phone)
        assertEquals(email, parsed.email)
        assertEquals(org, parsed.organization)
    }

    @Test
    fun formatContactQrText_roundTrip_nameWithSpecialChars() {
        val name = "O'Brien; Smith"
        val qrText = formatContactQrText(name, null, null, null)
        val parsed = parseContactVCard(qrText)
        assertEquals(name, parsed.name)
    }

    @Test
    fun formatContactQrText_roundTrip_orgWithSpecialChars() {
        val org = "ACME\\Corp, Inc."
        val qrText = formatContactQrText("Test", null, null, org)
        val parsed = parseContactVCard(qrText)
        assertEquals(org, parsed.organization)
    }

    @Test
    fun parseContactVCard_escapedValues_unescapesName() {
        val content = "BEGIN:VCARD\nVERSION:3.0\nFN:O\\;Brien\nEND:VCARD"
        val result = parseContactVCard(content)
        assertEquals("O;Brien", result.name)
    }

    @Test
    fun parseContactVCard_escapedValues_unescapesBackslash() {
        val content = "BEGIN:VCARD\nVERSION:3.0\nFN:Back\\\\slash\nEND:VCARD"
        val result = parseContactVCard(content)
        assertEquals("Back\\slash", result.name)
    }

    @Test
    fun parseContactVCard_escapedNewlineInOrg_unescapesNewline() {
        val content = "BEGIN:VCARD\nVERSION:3.0\nFN:Test\nORG:Line\\nTwo\nEND:VCARD"
        val result = parseContactVCard(content)
        assertEquals("Line\nTwo", result.organization)
    }
}
