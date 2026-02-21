package cat.company.qrreader.utils

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Utility functions for barcode type and format conversion
 */

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
