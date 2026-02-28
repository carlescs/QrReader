package cat.company.qrreader.core.camera

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cat.company.qrreader.core.camera.analyzer.BarcodeAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera preview
 *
 * @param isTorchOn Whether the torch/flashlight should be enabled.
 * @param onHasFlashUnit Called once when the camera is bound to report whether the device has
 *   a flash unit available for the back camera.
 * @param notifyBarcode Called with the list of detected barcodes when the user taps the preview.
 */
@Composable
@ExperimentalGetImage
@TransformExperimental
fun CameraPreview(
    isTorchOn: Boolean = false,
    onHasFlashUnit: (Boolean) -> Unit = {},
    notifyBarcode: ((List<Barcode>?) -> Unit)?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var savedBarcodes by remember { mutableStateOf<List<Barcode>?>(null) }

    // Enable or disable the torch whenever isTorchOn or the camera reference changes.
    LaunchedEffect(isTorchOn, camera) {
        camera?.cameraControl?.enableTorch(isTorchOn)
    }

    AndroidView(
        factory = { androidViewContext ->
            PreviewView(androidViewContext).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier
            .fillMaxSize(),
        update = { previewView ->
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)

            previewView.setOnClickListener {_ ->
                notifyBarcode?.invoke(savedBarcodes)
            }
            cameraProviderFuture.addListener({
                preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val barcodeAnalyser = BarcodeAnalyzer { barcodes ->
                    val target = previewView.outputTransform ?: return@BarcodeAnalyzer
                    val coordinateTransform = CoordinateTransform(barcodes.source, target)
                    previewView.overlay.clear()
                    barcodes.barcodes.forEach {
                        previewView.overlay.add(QrCodeDrawable(it,coordinateTransform))
                    }
                    savedBarcodes=barcodes.barcodes
                }
                val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                    }

                try {
                    cameraProvider.unbindAll()
                    val boundCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    camera = boundCamera
                    onHasFlashUnit(boundCamera.cameraInfo.hasFlashUnit())
                } catch (e: Exception) {
                    Log.d("TAG", "CameraPreview: ${e.localizedMessage}")
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}