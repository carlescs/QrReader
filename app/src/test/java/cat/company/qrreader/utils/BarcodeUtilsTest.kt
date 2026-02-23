package cat.company.qrreader.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
