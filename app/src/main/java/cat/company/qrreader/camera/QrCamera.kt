package cat.company.qrreader.camera

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cat.company.qrreader.camera.bottomSheet.BottomSheetContent
import cat.company.qrreader.db.BarcodesDb
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class,)
@ExperimentalGetImage
@Composable
fun QrCamera(db: BarcodesDb, snackbarHostState: SnackbarHostState, viewModel: QrCameraViewModel = QrCameraViewModel()){
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit){
        bottomSheetState.hide()
    }
    ModalBottomSheetLayout(
        sheetShape = RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp),
        sheetContent = {
            BottomSheetContent(lastBarcode = state.lastBarcode, db = db,snackbarHostState)
        },
        sheetState = bottomSheetState,
        scrimColor = Color.DarkGray.copy(alpha = 0.8f)
    ) {
        if(!permissionState.status.isGranted)
            permissionState.launchPermissionRequest()
        else{
            CameraPreview {
                if (!bottomSheetState.isVisible) {
                    viewModel.saveBarcodes(it)
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