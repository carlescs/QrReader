package cat.company.qrreader.history

import cat.company.qrreader.db.entities.SavedBarcode
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Get the title of a barcode
 */
fun getTitle(barcode: SavedBarcode): String {
    var title = when (barcode.type) {
        Barcode.TYPE_URL -> "URL"
        else ->
            when (barcode.format) {
                Barcode.FORMAT_EAN_13 -> "EAN13"
                else -> "Other"
            }
    }
    if (barcode.title != null && barcode.title!!.trim() != "")
        title = "$title: ${barcode.title}"
    return title
}