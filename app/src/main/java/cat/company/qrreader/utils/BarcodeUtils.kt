package cat.company.qrreader.utils

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Utility functions for barcode type and format conversion
 */

/**
 * Parsed WiFi credentials extracted from a raw WIFI: QR string.
 *
 * @property ssid The network name (SSID), or null if not present.
 * @property password The network password, or null for open networks.
 * @property securityType The security type string (e.g. "WPA", "WEP", "nopass"), or null.
 */
data class WifiInfo(val ssid: String?, val password: String?, val securityType: String?)

/**
 * Parsed contact information extracted from a raw vCard string.
 *
 * @property name The formatted display name, or null if not present.
 * @property phone The first phone number, or null if not present.
 * @property email The first email address, or null if not present.
 * @property organization The organization/company name, or null if not present.
 */
data class ContactInfo(
    val name: String?,
    val phone: String?,
    val email: String?,
    val organization: String?
)

/**
 * Parses a raw vCard string and returns the extracted [ContactInfo].
 * Supports common vCard 2.1 and 3.0 fields: FN, N, TEL, EMAIL, ORG.
 */
fun parseContactVCard(content: String): ContactInfo {
    val lines = content.lines()
    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var organization: String? = null

    for (line in lines) {
        when {
            line.startsWith("FN:") -> name = line.removePrefix("FN:").trim()
            line.startsWith("N:") && name == null -> {
                val parts = line.removePrefix("N:").split(";")
                val last = parts.getOrNull(0)?.trim()
                val first = parts.getOrNull(1)?.trim()
                name = listOfNotNull(
                    first?.takeIf { it.isNotEmpty() },
                    last?.takeIf { it.isNotEmpty() }
                ).joinToString(" ").takeIf { it.isNotEmpty() }
            }
            line.startsWith("TEL") && phone == null -> phone = line.substringAfter(":").trim()
            line.startsWith("EMAIL") && email == null -> email = line.substringAfter(":").trim()
            line.startsWith("ORG:") -> organization = line.removePrefix("ORG:").trim()
        }
    }

    return ContactInfo(
        name = name?.takeIf { it.isNotEmpty() },
        phone = phone?.takeIf { it.isNotEmpty() },
        email = email?.takeIf { it.isNotEmpty() },
        organization = organization?.takeIf { it.isNotEmpty() }
    )
}


/**
 * Parses a raw WiFi QR code string in the format `WIFI:T:<type>;S:<ssid>;P:<password>;;`
 * and returns the extracted [WifiInfo].
 */
fun parseWifiContent(content: String): WifiInfo {
    val ssid = Regex("S:([^;]+)").find(content)?.groupValues?.getOrNull(1)
    val password = Regex("P:([^;]*)").find(content)?.groupValues?.getOrNull(1)?.takeIf { it.isNotEmpty() }
    val securityType = Regex("T:([^;]+)").find(content)?.groupValues?.getOrNull(1)
    return WifiInfo(ssid, password, securityType)
}

/**
 * Convert ML Kit barcode value type to a human-readable name.
 */
fun getBarcodeTypeName(valueType: Int): String = when (valueType) {
    Barcode.TYPE_CONTACT_INFO -> "Contact"
    Barcode.TYPE_EMAIL -> "Email"
    Barcode.TYPE_ISBN -> "ISBN"
    Barcode.TYPE_PHONE -> "Phone"
    Barcode.TYPE_PRODUCT -> "Product"
    Barcode.TYPE_SMS -> "SMS"
    Barcode.TYPE_TEXT -> "Text"
    Barcode.TYPE_URL -> "URL"
    Barcode.TYPE_WIFI -> "Wi-Fi"
    Barcode.TYPE_GEO -> "Location"
    Barcode.TYPE_CALENDAR_EVENT -> "Calendar"
    Barcode.TYPE_DRIVER_LICENSE -> "Driver License"
    else -> "Unknown"
}

/**
 * Convert ML Kit barcode format to a human-readable name.
 */
fun getBarcodeFormatName(format: Int): String = when (format) {
    Barcode.FORMAT_QR_CODE -> "QR Code"
    Barcode.FORMAT_AZTEC -> "Aztec"
    Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
    Barcode.FORMAT_PDF417 -> "PDF417"
    Barcode.FORMAT_EAN_13 -> "EAN-13"
    Barcode.FORMAT_EAN_8 -> "EAN-8"
    Barcode.FORMAT_UPC_A -> "UPC-A"
    Barcode.FORMAT_UPC_E -> "UPC-E"
    Barcode.FORMAT_CODE_39 -> "Code 39"
    Barcode.FORMAT_CODE_93 -> "Code 93"
    Barcode.FORMAT_CODE_128 -> "Code 128"
    Barcode.FORMAT_CODABAR -> "Codabar"
    Barcode.FORMAT_ITF -> "ITF"
    else -> "Unknown"
}
