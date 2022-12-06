package cat.company.qrreader

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cat.company.qrreader.camera.QrCamera
import cat.company.qrreader.drawer.DrawerItem
import cat.company.qrreader.history.History
import cat.company.qrreader.ui.theme.QrReaderTheme
import kotlinx.coroutines.launch

@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    val scaffoldState = rememberScaffoldState()
                    val coroutineScope= rememberCoroutineScope()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = { TopAppBar(
                            title = { Text(text = "QrReader") },
                            navigationIcon = { IconButton(onClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    modifier = Modifier
                                        .padding(start = 8.dp),
                                    contentDescription = "Main menu"
                                )
                            }}
                        ) },
                        drawerContent = {
                            DrawerItem(title = "Camera", route = "camera", navController = navController, drawerState = scaffoldState.drawerState, selected = currentRoute=="camera")
                            DrawerItem(title = "History", route = "history", navController = navController, drawerState = scaffoldState.drawerState, selected = currentRoute=="history")
                        }
                    ) {
                        NavHost(navController = navController, startDestination = "camera") {
                            composable("camera") { QrCamera() }
                            composable("history") { History() }
                        }
                    }
                }
            }
        }
    }
}