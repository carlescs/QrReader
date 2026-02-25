package cat.company.qrreader.features.history.presentation.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.FileProvider
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.utils.parseContactVCard
import cat.company.qrreader.utils.parseWifiContent
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.File

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
 * Share a barcode via the system share sheet.
 * - Contact barcodes are shared as a .vcf vCard file.
 * - WiFi barcodes are shared as a human-readable plain-text summary.
 * - All other types are shared as plain text.
 */
fun shareBarcode(context: Context, barcode: BarcodeModel) {
    when (barcode.type) {
        Barcode.TYPE_CONTACT_INFO -> {
            val vCardContent = if (barcode.barcode.trimStart().startsWith("BEGIN:VCARD", ignoreCase = true)) {
                barcode.barcode
            } else {
                val contactInfo = parseContactVCard(barcode.barcode)
                buildString {
                    appendLine("BEGIN:VCARD")
                    appendLine("VERSION:3.0")
                    contactInfo.name?.let { appendLine("FN:$it") }
                    contactInfo.phone?.let { appendLine("TEL:$it") }
                    contactInfo.email?.let { appendLine("EMAIL:$it") }
                    contactInfo.organization?.let { appendLine("ORG:$it") }
                    append("END:VCARD")
                }
            }
            val contactsDir = File(context.cacheDir, "contacts")
            contactsDir.mkdirs()
            val vcfFile = File(contactsDir, "contact.vcf")
            vcfFile.writeText(vCardContent)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", vcfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/x-vcard"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
        Barcode.TYPE_WIFI -> {
            val wifiInfo = parseWifiContent(barcode.barcode)
            val shareText = buildString {
                wifiInfo.ssid?.let { appendLine(context.getString(R.string.wifi_ssid, it)) }
                wifiInfo.password?.let { appendLine(context.getString(R.string.wifi_password, it)) }
                wifiInfo.securityType?.let { append(context.getString(R.string.wifi_security, it)) }
            }.ifEmpty { barcode.barcode }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
        else -> {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, barcode.barcode)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
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

