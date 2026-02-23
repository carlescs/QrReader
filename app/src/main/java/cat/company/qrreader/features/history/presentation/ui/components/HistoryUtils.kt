package cat.company.qrreader.features.history.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import cat.company.qrreader.domain.model.BarcodeModel
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Get icon for barcode type
 */
fun getBarcodeIcon(type: Int): ImageVector {
    return when (type) {
        Barcode.TYPE_URL -> Icons.Filled.Link
        Barcode.TYPE_CONTACT_INFO -> Icons.Filled.AccountBox
        Barcode.TYPE_EMAIL -> Icons.Filled.Email
        Barcode.TYPE_PHONE -> Icons.Filled.Phone
        Barcode.TYPE_WIFI -> Icons.Filled.Wifi
        else -> Icons.Filled.QrCode
    }
}

/**
 * Get the title of a barcode
 */
fun getTitle(barcode: BarcodeModel): String {
    if (barcode.title != null && barcode.title.trim() != "")
        return barcode.title

    // Return a default based on type if no custom title
    return when (barcode.type) {
        Barcode.TYPE_URL -> "URL"
        Barcode.TYPE_CONTACT_INFO -> "Contact"
        Barcode.TYPE_EMAIL -> "Email"
        Barcode.TYPE_PHONE -> "Phone"
        Barcode.TYPE_WIFI -> "Wi-Fi"
        else ->
            when (barcode.format) {
                Barcode.FORMAT_EAN_13 -> "EAN13"
                Barcode.FORMAT_EAN_8 -> "EAN8"
                Barcode.FORMAT_UPC_A -> "UPC-A"
                Barcode.FORMAT_UPC_E -> "UPC-E"
                else -> "Barcode"
            }
    }
}

