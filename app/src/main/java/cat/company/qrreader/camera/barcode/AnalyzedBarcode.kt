package cat.company.qrreader.camera.barcode

import androidx.camera.view.transform.OutputTransform
import com.google.mlkit.vision.barcode.common.Barcode

data class AnalyzedBarcode(
    val barcodes: List<Barcode>,
    val source: OutputTransform
)