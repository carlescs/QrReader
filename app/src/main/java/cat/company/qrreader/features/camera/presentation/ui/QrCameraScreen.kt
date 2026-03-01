package cat.company.qrreader.features.camera.presentation.ui

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.core.camera.CameraPreview
import cat.company.qrreader.domain.usecase.camera.ScanImageUseCase
import cat.company.qrreader.features.camera.presentation.QrCameraViewModel
import cat.company.qrreader.features.camera.presentation.ui.components.BottomSheetContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

/**
 * QR Camera Screen - Display camera preview and scan barcodes
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@ExperimentalGetImage
@ExperimentalMaterial3Api
@Composable
fun QrCameraScreen(
    snackbarHostState: SnackbarHostState,
    sharedImageUri: Uri? = null,
    onSharedImageConsumed: () -> Unit = {},
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {},
    sharedContactText: String? = null,
    onSharedContactTextConsumed: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    viewModel: QrCameraViewModel = koinViewModel()
) {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scanImageUseCase: ScanImageUseCase = koinInject()
    val noBarcodes = stringResource(R.string.no_barcodes_found)
    val imageProcessingFailed = stringResource(R.string.image_processing_failed)
    // isTorchOn is saved across configuration changes (e.g. rotation) so the user's choice
    // is preserved when the screen is recreated. hasFlashUnit is not saved because it is
    // re-detected from the hardware after each camera binding.
    var isTorchOn by rememberSaveable { mutableStateOf(false) }
    var hasFlashUnit by remember { mutableStateOf(false) }

    suspend fun scanUriAndShowResult(uri: Uri) {
        try {
            val barcodes = scanImageUseCase(context, uri)
            if (barcodes.isNotEmpty()) {
                viewModel.saveBarcodes(barcodes)
                openBottomSheet = true
                bottomSheetState.show()
            } else {
                snackbarHostState.showSnackbar(noBarcodes)
            }
        } catch (e: Exception) {
            Log.e("QrCameraScreen", "Failed to process image", e)
            snackbarHostState.showSnackbar(imageProcessingFailed)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch { scanUriAndShowResult(uri) }
        }
    }

    // Hide the bottom sheet on initial composition only when there is no shared content
    // about to be displayed. If sharedText or sharedContactText is already set, skipping
    // hide() avoids a race with the show() call in the effects below, which would cause
    // the bottom sheet to be hidden right after it is opened for the shared content.
    //
    // openBottomSheet is set to false BEFORE hide() so that if sharedText or
    // sharedContactText arrives while hide() is suspended (animating), the show() call
    // in LaunchedEffect(sharedText/sharedContactText) sets openBottomSheet = true AFTER
    // our false, ensuring the sheet remains visible.
    LaunchedEffect(Unit) {
        if (sharedText == null && sharedContactText == null) {
            openBottomSheet = false
            bottomSheetState.hide()
        }
    }

    // Scan the shared image when a URI is provided
    LaunchedEffect(sharedImageUri) {
        if (sharedImageUri != null) {
            scanUriAndShowResult(sharedImageUri)
            onSharedImageConsumed()
        }
    }

    // Show shared text (e.g. WiFi QR string) as if it was scanned from a barcode
    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            viewModel.setSharedWifiText(sharedText)
            openBottomSheet = true
            bottomSheetState.show()
            onSharedTextConsumed()
        }
    }

    // Show shared contact (vCard) as if it was scanned from a barcode
    LaunchedEffect(sharedContactText) {
        if (sharedContactText != null) {
            viewModel.setSharedContactText(sharedContactText)
            openBottomSheet = true
            bottomSheetState.show()
            onSharedContactTextConsumed()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!permissionState.status.isGranted) {
            PermissionPrompt(
                permissionState = permissionState,
                onPickImage = { imagePickerLauncher.launch("image/*") }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    isTorchOn = isTorchOn,
                    onHasFlashUnit = { hasFlashUnit = it }
                ) {
                    if (it?.isNotEmpty() == true && !openBottomSheet) {
                        openBottomSheet = true
                        viewModel.saveBarcodes(it)
                        coroutineScope.launch {
                            bottomSheetState.show()
                        }
                    }
                }
                CameraOverlayButtons(
                    hasFlashUnit = hasFlashUnit,
                    isTorchOn = isTorchOn,
                    onTorchToggle = { isTorchOn = !isTorchOn },
                    onPickImage = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
        BackHandler(enabled = openBottomSheet) {
            coroutineScope.launch {
                bottomSheetState.hide()
            }.invokeOnCompletion {
                if (!bottomSheetState.isVisible) openBottomSheet = false
            }
        }

        if (openBottomSheet) {
            ModalBottomSheet(
                shape = RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp),
                sheetState = bottomSheetState,
                onDismissRequest = {
                    openBottomSheet = false
                },
                scrimColor = Color.DarkGray.copy(alpha = 0.8f)
            ) {
                BottomSheetContent(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onToggleTag = { barcodeHash, tagName ->
                        viewModel.toggleTagSelection(barcodeHash, tagName)
                    },
                    onNavigateToHistory = onNavigateToHistory
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun PermissionPrompt(permissionState: PermissionState, onPickImage: () -> Unit) {
    Column(
        Modifier.padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textToShow = if (permissionState.status.shouldShowRationale) {
            stringResource(R.string.camera_permissions_rationale)
        } else {
            stringResource(R.string.camera_permission_request)
        }
        Text(textToShow, Modifier.padding(0.dp, 20.dp))
        Button(onClick = { permissionState.launchPermissionRequest() }) {
            Text(stringResource(R.string.request_permission))
        }
        Button(onClick = onPickImage, Modifier.padding(top = 8.dp)) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.upload_image))
        }
    }
}

/**
 * Overlay buttons shown on the camera preview: an optional torch toggle (only when a flash unit
 * is present) and an upload-image button.
 *
 * @param hasFlashUnit Whether the current device has a hardware flash unit.
 * @param isTorchOn Whether the torch is currently on.
 * @param onTorchToggle Called when the user taps the torch button.
 * @param onPickImage Called when the user taps the image-picker button.
 * @param modifier Modifier applied to the containing [Column].
 */
@Composable
internal fun CameraOverlayButtons(
    hasFlashUnit: Boolean,
    isTorchOn: Boolean,
    onTorchToggle: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasFlashUnit) {
            FilledIconButton(onClick = onTorchToggle) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                    contentDescription = stringResource(
                        if (isTorchOn) R.string.turn_torch_off else R.string.turn_torch_on
                    )
                )
            }
        }
        FilledIconButton(onClick = onPickImage) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = stringResource(R.string.upload_image)
            )
        }
    }
}

