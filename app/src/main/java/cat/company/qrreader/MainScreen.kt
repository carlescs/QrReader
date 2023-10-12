package cat.company.qrreader

import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cat.company.qrreader.camera.QrCamera
import cat.company.qrreader.codeCreator.CodeCreator
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.drawer.DrawerContent
import cat.company.qrreader.events.SharedEvents
import cat.company.qrreader.history.History
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalGetImage
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(db: BarcodesDb) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val coroutineScope = rememberCoroutineScope()
        val navController = rememberNavController()
        val sidebarState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val snackBarHostState = remember { SnackbarHostState() }
        val currentRoute = navController
            .currentBackStackEntryFlow
            .collectAsState(initial = navController.currentBackStackEntry)
        val currentBackStackEntry by navController.currentBackStackEntryAsState()

        val showBackButton by remember(currentBackStackEntry) {
            derivedStateOf {
                navController.previousBackStackEntry != null && currentBackStackEntry?.destination?.route.equals("camera")
            }
        }
        ModalNavigationDrawer(drawerContent = {
            DrawerContent(navController, coroutineScope, sidebarState)
        }, drawerState = sidebarState, gesturesEnabled = sidebarState.isOpen) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackBarHostState) },
                topBar = {
                    TopAppBar(
                        showBackButton,
                        navController,
                        coroutineScope,
                        sidebarState,
                        currentRoute
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(currentRoute, navController)
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    NavHost(navController = navController, startDestination = "history") {
                        composable("camera") { QrCamera(db, snackBarHostState) }
                        composable("history") { History(db, snackBarHostState) }
                        composable("codeCreator"){ CodeCreator() }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopAppBar(
    showBackButton: Boolean,
    navController: NavHostController,
    coroutineScope: CoroutineScope,
    sidebarState: DrawerState,
    currentRoute: State<NavBackStackEntry?>
) {
    val shareDisabled=remember { mutableStateOf(false) }
    SharedEvents.onShareIsDisabled= {
        shareDisabled.value = it
    }
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
            } else {
                IconButton(onClick = {
                    coroutineScope.launch { sidebarState.open() }
                }) {
                    Icon(
                        // internal hamburger menu
                        Icons.Rounded.Menu,
                        contentDescription = "MenuButton"
                    )
                }
            }
        },
        actions = {
            if (currentRoute.value?.destination?.route.equals("codeCreator")) {
                IconButton(onClick = {
                    if(!shareDisabled.value) {
                        shareDisabled.value = true
                        try {
                            SharedEvents.onShareClick?.invoke()
                        }
                        finally {
                            shareDisabled.value= false
                        }
                    }
                }, enabled = !shareDisabled.value) {
                    Icon(Icons.Filled.Share, contentDescription = "Share")
                }
            }
        }
    )
}

@Composable
private fun FloatingActionButton(
    currentRoute: State<NavBackStackEntry?>,
    navController: NavHostController
) {
    if (currentRoute.value?.destination?.route.equals("history"))
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
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
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

