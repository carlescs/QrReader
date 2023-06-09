package cat.company.qrreader.camera

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cat.company.qrreader.camera.bottomSheet.BottomSheetContent
import cat.company.qrreader.db.BarcodesDb
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@ExperimentalGetImage
@ExperimentalMaterial3Api
@Composable
fun QrCamera(db: BarcodesDb, snackbarHostState: SnackbarHostState, viewModel: QrCameraViewModel = QrCameraViewModel()){
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val coroutineScope = rememberCoroutineScope()
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit){
        bottomSheetState.hide()
    }
    Column(modifier=Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center){
        if(!permissionState.status.isGranted)
            permissionState.launchPermissionRequest()
        else{
            CameraPreview {
                if (it?.isNotEmpty() == true)
                    if (!bottomSheetState.isVisible) {
                        viewModel.saveBarcodes(it)
                        coroutineScope.launch {
                            bottomSheetState.show()
                            openBottomSheet=true
                        }
                    }
            }
        }
        BackHandler(enabled = bottomSheetState.isVisible) {
            coroutineScope.launch {
                bottomSheetState.hide()
                openBottomSheet=false
            }
        }
        if(openBottomSheet) {
            ModalBottomSheet(
                shape = RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp),
                content = {
                    BottomSheetContent(lastBarcode = state.lastBarcode, db = db, snackbarHostState)
                },
                sheetState = bottomSheetState,
                onDismissRequest = {
                    coroutineScope.launch {
                        bottomSheetState.hide()
                        openBottomSheet=false
                    }
                },
                scrimColor = Color.DarkGray.copy(alpha = 0.8f)
            )
        }
    }
}