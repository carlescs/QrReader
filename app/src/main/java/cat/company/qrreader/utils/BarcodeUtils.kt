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
 * Parses a raw vCard or MECARD string and returns the extracted [ContactInfo].
 * Supports common vCard 2.1 and 3.0 fields (FN, N, TEL, EMAIL, ORG) including
 * parameterized keys (e.g. `TEL;TYPE=CELL:`) and folded lines, as well as MECARD format.
 */
fun parseContactVCard(content: String): ContactInfo {
    if (content.trimStart().startsWith("MECARD:", ignoreCase = true)) {
        return parseMecardContact(content)
    }

    // Unfold folded vCard lines (lines starting with space/tab continue the previous line).
    val rawLines = content.lines()
    val lines = mutableListOf<String>()
    for (rawLine in rawLines) {
        if (rawLine.isEmpty()) continue
        if ((rawLine.startsWith(" ") || rawLine.startsWith("\t")) && lines.isNotEmpty()) {
            lines[lines.lastIndex] = lines.last() + rawLine.trimStart()
        } else {
            lines += rawLine
        }
    }

    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var organization: String? = null

    for (line in lines) {
        if (!line.contains(':')) continue
        val key = line.substringBefore(':').substringBefore(';').uppercase()
        val value = line.substringAfter(':').trim()

        when (key) {
            "FN" -> if (value.isNotEmpty()) name = value
            "N" -> if (name == null && value.isNotEmpty()) {
                val parts = value.split(";")
                val last = parts.getOrNull(0)?.trim()
                val first = parts.getOrNull(1)?.trim()
                name = listOfNotNull(
                    first?.takeIf { it.isNotEmpty() },
                    last?.takeIf { it.isNotEmpty() }
                ).joinToString(" ").takeIf { it.isNotEmpty() }
            }
            "TEL" -> if (phone == null && value.isNotEmpty()) phone = value
            "EMAIL" -> if (email == null && value.isNotEmpty()) email = value
            "ORG" -> if (value.isNotEmpty()) organization = value
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
 * Parses a MECARD-formatted contact string and returns the extracted [ContactInfo].
 *
 * Example: `MECARD:N:John Doe;TEL:123456789;EMAIL:john@example.com;ORG:Example Corp;;`
 */
private fun parseMecardContact(content: String): ContactInfo {
    val body = content.substringAfter(":", missingDelimiterValue = "")
    val fields = body.split(';')

    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var organization: String? = null

    for (field in fields) {
        if (field.isEmpty()) continue
        val key = field.substringBefore(':').uppercase()
        val value = field.substringAfter(':', missingDelimiterValue = "").trim()

        when (key) {
            "N", "FN" -> if (name == null && value.isNotEmpty()) name = value
            "TEL" -> if (phone == null && value.isNotEmpty()) phone = value
            "EMAIL" -> if (email == null && value.isNotEmpty()) email = value
            "ORG" -> if (organization == null && value.isNotEmpty()) organization = value
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
 * Escapes special characters in a vCard property value per RFC 2426.
 *
 * The characters `\`, `;`, `,`, and newlines must be escaped with a preceding backslash.
 */
private fun escapeVCardValue(value: String): String =
    value.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")

/**
 * Formats contact information into a vCard 3.0 QR-code-compatible string.
 *
 * Produces a string in the standard `BEGIN:VCARD / END:VCARD` format (version 3.0)
 * recognised by most QR code scanners and Android's built-in Contacts app.
 * Special characters in field values are escaped per RFC 2426.
 *
 * @param name The full display name (FN field). Must not be empty.
 * @param phone Optional phone number (TEL field).
 * @param email Optional email address (EMAIL field).
 * @param organization Optional organisation / company name (ORG field).
 * @return The formatted vCard 3.0 text suitable for encoding in a QR code.
 */
fun formatContactQrText(
    name: String,
    phone: String?,
    email: String?,
    organization: String?
): String = buildString {
    appendLine("BEGIN:VCARD")
    appendLine("VERSION:3.0")
    appendLine("FN:${escapeVCardValue(name)}")
    if (!phone.isNullOrEmpty()) appendLine("TEL:${escapeVCardValue(phone)}")
    if (!email.isNullOrEmpty()) appendLine("EMAIL:${escapeVCardValue(email)}")
    if (!organization.isNullOrEmpty()) appendLine("ORG:${escapeVCardValue(organization)}")
    append("END:VCARD")
}

/**
 * Escapes special characters in a WiFi QR code field value.
 *
 * According to the WiFi QR code format specification, the characters
 * `\`, `;`, `,`, `"`, and `:` must be escaped with a preceding backslash
 * when they appear in SSID or password values.
 */
private fun escapeWifiValue(value: String): String =
    value.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\"", "\\\"")
        .replace(":", "\\:")

/**
 * Formats WiFi credentials into a QR-code-compatible string.
 *
 * Produces a string in the standard `WIFI:T:<type>;S:<ssid>;P:<password>;;` format
 * used by most QR code scanners and Android's built-in WiFi QR feature.
 * Special characters in the SSID and password are escaped per the specification.
 *
 * @param ssid The network name (SSID).
 * @param password The network password. Ignored when [securityType] is `"nopass"`.
 * @param securityType The security protocol: `"WPA"`, `"WEP"`, or `"nopass"` for open networks.
 * @return The formatted WiFi QR code text.
 */
fun formatWifiQrText(ssid: String, password: String?, securityType: String): String {
    return buildString {
        append("WIFI:T:$securityType;S:${escapeWifiValue(ssid)};")
        if (securityType != "nopass" && !password.isNullOrEmpty()) {
            append("P:${escapeWifiValue(password)};")
        }
        append(";")
    }
}

/**
 * Unescapes special characters in a WiFi QR code field value.
 *
 * Reverses the escaping applied by [escapeWifiValue]: a backslash followed by
 * any character is replaced by that character alone (e.g., `\;` → `;`, `\\` → `\`).
 */
private fun unescapeWifiValue(value: String): String = buildString {
    var i = 0
    while (i < value.length) {
        if (value[i] == '\\' && i + 1 < value.length) {
            append(value[i + 1])
            i += 2
        } else {
            append(value[i])
            i++
        }
    }
}

/**
 * Parses a raw WiFi QR code string in the format `WIFI:T:<type>;S:<ssid>;P:<password>;;`
 * and returns the extracted [WifiInfo].
 *
 * Field values may contain escaped characters (e.g., `\;` for a literal semicolon).
 * This function correctly handles such values by matching escaped sequences before
 * treating `;` as a delimiter, then unescaping the extracted values.
 */
fun parseWifiContent(content: String): WifiInfo {
    // Pattern (?:\\.|[^;])+ matches a sequence of either an escaped character (\X)
    // or any non-semicolon character, so escaped semicolons are not treated as delimiters.
    val ssid = Regex("S:((?:\\\\.|[^;])+)").find(content)?.groupValues?.getOrNull(1)
        ?.let { unescapeWifiValue(it) }
    val password = Regex("P:((?:\\\\.|[^;])*)").find(content)?.groupValues?.getOrNull(1)
        ?.takeIf { it.isNotEmpty() }?.let { unescapeWifiValue(it) }
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
