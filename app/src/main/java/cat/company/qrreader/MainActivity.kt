package cat.company.qrreader

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import cat.company.qrreader.camera.QrCamera
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.Migrations
import cat.company.qrreader.history.History
import cat.company.qrreader.ui.theme.QrReaderTheme

@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room
            .databaseBuilder(applicationContext, BarcodesDb::class.java, "barcodes_db")
            .addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3
            )
            .build()
        setContent {
            QrReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val currentRoute = navController
                        .currentBackStackEntryFlow
                        .collectAsState(initial = navController.currentBackStackEntry)
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()

                    val showBackButton by remember(currentBackStackEntry) {
                        derivedStateOf {
                            navController.previousBackStackEntry != null
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text(stringResource(id = R.string.app_name)) },
                                navigationIcon =
                                {
                                    if (showBackButton) {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        floatingActionButton = {

                            if (currentRoute.value?.destination?.route != "camera")
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate("camera") {
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
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_qr_code_scanner_24),
                                        contentDescription = stringResource(id = R.string.scan_qr_code)
                                    )
                                }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                        ) {
                            NavHost(navController = navController, startDestination = "history") {
                                composable("camera") { QrCamera(db, snackbarHostState) }
                                composable("history") { History(db, snackbarHostState) }
                            }
                        }
                    }
                }
            }
        }
    }
}