package cat.company.qrreader.domain.usecase.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Use case to scan barcodes from an image URI using ML Kit
 */
class ScanImageUseCase {

    companion object {
        private const val TAG = "ScanImageUseCase"
    }

    suspend operator fun invoke(context: Context, uri: Uri): List<Barcode> =
        withContext(Dispatchers.IO) {
            val inputImage = InputImage.fromFilePath(context, uri)
            suspendCoroutine { continuation ->
                val barcodeScanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                )
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        barcodeScanner.close()
                        continuation.resume(barcodes)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Barcode scanning failed", exception)
                        barcodeScanner.close()
                        continuation.resumeWithException(exception)
                    }
            }
        }
}
