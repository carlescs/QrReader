package cat.company.qrreader.features.camera.presentation.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.core.camera.CameraPreview
import cat.company.qrreader.features.camera.presentation.QrCameraViewModel
import cat.company.qrreader.features.camera.presentation.ui.components.BottomSheetContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
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

    LaunchedEffect(Unit) {
        bottomSheetState.hide()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!permissionState.status.isGranted) {
            PermissionPrompt(permissionState)
        } else {
            CameraPreview {
                if (it?.isNotEmpty() == true && !openBottomSheet) {
                    openBottomSheet = true
                    viewModel.saveBarcodes(it)
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
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
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun PermissionPrompt(permissionState: PermissionState) {
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
            Text("Request permission")
        }
    }
}

