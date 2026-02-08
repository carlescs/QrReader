package cat.company.qrreader.core.camera.analyzer

import androidx.annotation.OptIn
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.OutputTransform
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Data class for analyzed barcode
 */
data class AnalyzedBarcode @OptIn(TransformExperimental::class) constructor
    (
    val barcodes: List<Barcode>,
    val source: OutputTransform
)