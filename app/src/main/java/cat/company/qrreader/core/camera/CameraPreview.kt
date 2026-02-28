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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cat.company.qrreader.core.camera.analyzer.BarcodeAnalyzer
import com.google.mlkit.vision.barcode.common.Barcode
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
    var camera by remember { mutableStateOf<Camera?>(null) }
    var savedBarcodes by remember { mutableStateOf<List<Barcode>?>(null) }
    val currentOnHasFlashUnit by rememberUpdatedState(onHasFlashUnit)
    // The executor is remembered separately so it isn't recreated if lifecycleOwner changes.
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    // Enable or disable the torch whenever isTorchOn or the camera reference changes.
    // Only call enableTorch when the device actually has a flash unit.
    LaunchedEffect(isTorchOn, camera) {
        val currentCamera = camera
        if (currentCamera != null && currentCamera.cameraInfo.hasFlashUnit()) {
            currentCamera.cameraControl.enableTorch(isTorchOn)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Bind the camera once per lifecycleOwner and tear it down on dispose.
    // Isolating binding from torch state changes prevents unnecessary re-binding when
    // the user toggles the torch (which would otherwise trigger AndroidView.update).
    DisposableEffect(lifecycleOwner) {
        var cameraProvider: ProcessCameraProvider? = null
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val provider = cameraProviderFuture.get()
            cameraProvider = provider
            val barcodeAnalyser = BarcodeAnalyzer { barcodes ->
                val target = previewView.outputTransform ?: return@BarcodeAnalyzer
                val coordinateTransform = CoordinateTransform(barcodes.source, target)
                previewView.overlay.clear()
                barcodes.barcodes.forEach {
                    previewView.overlay.add(QrCodeDrawable(it, coordinateTransform))
                }
                savedBarcodes = barcodes.barcodes
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(cameraExecutor, barcodeAnalyser) }

            try {
                provider.unbindAll()
                val boundCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                camera = boundCamera
                currentOnHasFlashUnit(boundCamera.cameraInfo.hasFlashUnit())
            } catch (e: Exception) {
                Log.d("TAG", "CameraPreview: ${e.localizedMessage}")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            view.setOnClickListener { _ -> notifyBarcode?.invoke(savedBarcodes) }
        }
    )
}