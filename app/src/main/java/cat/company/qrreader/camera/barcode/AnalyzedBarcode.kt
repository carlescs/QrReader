package cat.company.qrreader.camera.barcode

import androidx.camera.view.transform.OutputTransform
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Data class for analyzed barcode
 */
data class AnalyzedBarcode(
    val barcodes: List<Barcode>,
    val source: OutputTransform
)