package cat.company.qrreader.domain.usecase.camera

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Use case to scan barcodes from an image URI using ML Kit
 */
class ScanImageUseCase {

    private val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    suspend operator fun invoke(context: Context, uri: Uri): List<Barcode> {
        return suspendCoroutine { continuation ->
            val inputImage = InputImage.fromFilePath(context, uri)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    continuation.resume(barcodes)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}
