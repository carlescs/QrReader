package cat.company.qrreader.camera

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.ui.unit.dp
import cat.company.qrreader.camera.bottomSheet.BottomSheetContent
import cat.company.qrreader.db.BarcodesDb
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@ExperimentalGetImage
@ExperimentalMaterial3Api
@Composable
fun QrCamera(db: BarcodesDb, snackbarHostState: SnackbarHostState, viewModel: QrCameraViewModel = viewModel()){
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit){
        bottomSheetState.hide()
    }
    Column(modifier=Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center){
        if(!permissionState.status.isGranted) {
            Column (Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally){
                val textToShow = if (permissionState.status.shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // then gently explain why the app requires this permission
                    "The camera is important for this app. Please grant the permission."
                } else {
                    // If it's the first time the user lands on this feature, or the user
                    // doesn't want to be asked again for this permission, explain that the
                    // permission is required
                    "Camera permission required for this feature to be available. " +
                            "Please grant the permission"
                }
                Text(textToShow, Modifier.padding(0.dp, 20.dp))
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                }
            }
        }
        else{
            CameraPreview {
                if (it?.isNotEmpty() == true)
                    if (!openBottomSheet) {
                        openBottomSheet= true
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
                if(!bottomSheetState.isVisible) openBottomSheet=false
            }
        }

        if(openBottomSheet) {
            ModalBottomSheet(
                shape = RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp),
                sheetState = bottomSheetState,
                onDismissRequest = {
                    openBottomSheet=false
                },
                scrimColor = Color.DarkGray.copy(alpha = 0.8f)
            ){
                BottomSheetContent(lastBarcode = state.lastBarcode, db = db, snackbarHostState)
            }
        }
    }
}