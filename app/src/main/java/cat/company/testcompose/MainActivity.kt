package cat.company.testcompose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cat.company.testcompose.camera.CameraPreview
import cat.company.testcompose.ui.theme.TestComposeTheme
import kotlinx.coroutines.launch

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
                    val scaffoldState= rememberScaffoldState()
                    val coroutineScope= rememberCoroutineScope()
                    Scaffold(
                        scaffoldState = scaffoldState
                    ) {
                        CameraPreview{
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(it.first().toString())
                            }
                        }
                    }
                }
            }
        }
    }
}