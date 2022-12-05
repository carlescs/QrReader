package cat.company.testcompose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cat.company.testcompose.bottomSheet.BottomSheetContent
import cat.company.testcompose.camera.CameraPreview
import cat.company.testcompose.ui.theme.TestComposeTheme
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val scaffoldState= rememberBottomSheetScaffoldState(bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed))
                    val coroutineScope= rememberCoroutineScope()
                    val lastBarcode:MutableState<List<Barcode>?> = remember{mutableStateOf(null)}
                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetContent = {
                            BottomSheetContent(lastBarcode = lastBarcode)
                        }
                    ) {
                        CameraPreview {
                            lastBarcode.value = it
                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                }
            }
        }
    }
}