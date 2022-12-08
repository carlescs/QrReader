package cat.company.qrreader.camera

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cat.company.qrreader.camera.bottomSheet.BottomSheetContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class,)
@ExperimentalGetImage
@Composable
fun QrCamera(vm:QrCameraViewModel= QrCameraViewModel()){
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val state by vm.uiState.collectAsState()

    ModalBottomSheetLayout(
        sheetShape = RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp),
        sheetContent = {
            BottomSheetContent(lastBarcode = state.lastBarcode)
        },
        sheetState = bottomSheetState,
        scrimColor = Color.DarkGray.copy(alpha = 0.8f)
    ) {
        if(!permissionState.hasPermission)
            permissionState.launchPermissionRequest()
        PermissionRequired(
            permissionState = permissionState,
            permissionNotGrantedContent = { Text(text = "You need to give permission to the camera.") },
            permissionNotAvailableContent = { Text(text = "No camera permission.") }) {
            CameraPreview {
                if (!bottomSheetState.isVisible) {
                    vm.saveBarcodes(it)
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
            }
        }
        BackHandler(enabled = bottomSheetState.isVisible) {
            coroutineScope.launch {
                bottomSheetState.hide()
            }
        }
    }
}