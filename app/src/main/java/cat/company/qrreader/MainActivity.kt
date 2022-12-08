package cat.company.qrreader

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import cat.company.qrreader.camera.QrCamera
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.history.History
import cat.company.qrreader.navigation.Screen
import cat.company.qrreader.ui.theme.QrReaderTheme

@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db= Room
            .databaseBuilder(applicationContext, BarcodesDb::class.java,"barcodes_db")
            .build()
        setContent {
            QrReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    val scaffoldState = rememberScaffoldState()
                    val items = listOf(
                        Screen.Camera,
                        Screen.History,
                    )
                    Scaffold(
                        scaffoldState = scaffoldState,
                        bottomBar = {
                            BottomNavigation {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination
                                items.forEach { screen ->
                                    BottomNavigationItem(
                                        icon = { Icon(painterResource(id = screen.icon), contentDescription = null) },
                                        label = { Text(stringResource(screen.resourceId)) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                // Pop up to the start destination of the graph to
                                                // avoid building up a large stack of destinations
                                                // on the back stack as users select items
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                // Avoid multiple copies of the same destination when
                                                // reselecting the same item
                                                launchSingleTop = true
                                                // Restore state when reselecting a previously selected item
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(it)) {
                            NavHost(navController = navController, startDestination = "camera") {
                                composable("camera") { QrCamera(db,scaffoldState.snackbarHostState) }
                                composable("history") { History(db,scaffoldState.snackbarHostState) }
                            }
                        }
                    }
                }
            }
        }
    }
}