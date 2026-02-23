package cat.company.qrreader

import android.net.Uri
import android.os.Bundle
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.navDeepLink
import cat.company.qrreader.features.camera.presentation.ui.QrCameraScreen
import cat.company.qrreader.features.codeCreator.presentation.ui.CodeCreatorScreen
import cat.company.qrreader.events.SharedEvents
import cat.company.qrreader.features.history.presentation.ui.History
import cat.company.qrreader.ui.components.navigation.items
import cat.company.qrreader.features.settings.presentation.ui.AiSettingsScreen
import cat.company.qrreader.features.settings.presentation.ui.HistorySettingsScreen
import cat.company.qrreader.features.settings.presentation.ui.SettingsScreen
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Main screen
 */
@ExperimentalGetImage
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(firebaseAnalytics: FirebaseAnalytics, sharedImageUri: Uri? = null, onSharedImageConsumed: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val params = Bundle()
            val screenName = when (destination.route) {
                "history" -> "History"
                "camera" -> "Camera"
                "codeCreator" -> "Code Creator"
                "settings" -> "Settings"
                "settings/history" -> "Settings - History"
                "settings/ai" -> "Settings - AI"
                else -> destination.route ?: "Unknown"
            }
            params.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
        }
        val snackBarHostState = remember { SnackbarHostState() }
        val currentRoute = navController
            .currentBackStackEntryFlow
            .collectAsState(initial = navController.currentBackStackEntry)
        val currentBackStackEntry by navController.currentBackStackEntryAsState()

        val showBackButton by remember(currentBackStackEntry) {
            derivedStateOf {
                navController.previousBackStackEntry != null
            }
        }

        // Navigate to camera screen when a shared image is received
        LaunchedEffect(sharedImageUri) {
            if (sharedImageUri != null) {
                navController.navigate("camera") {
                    launchSingleTop = true
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = {
                TopAppBar(
                    showBackButton,
                    navController,
                    currentRoute
                )
            },
            bottomBar = {
                val activeRoute=navController.currentBackStackEntryFlow.collectAsState(initial=navController.currentBackStackEntry)
                if(activeRoute.value?.destination?.route !="camera") {
                    NavigationBar{
                        items.forEach { item ->
                            NavigationBarItem(
                                icon = item.icon,
                                label = { Text(stringResource(item.labelRes)) },
                                selected = activeRoute.value?.destination?.route == item.route,
                                onClick = {
                                    navController.navigate(item.route){
                                        popUpTo(navController.graph.findStartDestination().id){
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
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
                    composable(
                        route = "camera",
                        deepLinks = listOf(navDeepLink { uriPattern = "qrreader://camera" })
                    ) {
                        QrCameraScreen(snackBarHostState, sharedImageUri = sharedImageUri, onSharedImageConsumed = onSharedImageConsumed)
                    }
                    composable("history") {
                        History(snackBarHostState)
                    }
                    composable(
                        route="codeCreator",
                        deepLinks = listOf(navDeepLink { uriPattern = "qrreader://codeCreator" })
                    ) {
                        CodeCreatorScreen()
                    }
                    composable("settings") {
                        SettingsScreen(
                            onNavigateToHistorySettings = {
                                navController.navigate("settings/history") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToAiSettings = {
                                navController.navigate("settings/ai") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable("settings/history") {
                        HistorySettingsScreen()
                    }
                    composable("settings/ai") {
                        AiSettingsScreen()
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
    currentRoute: State<NavBackStackEntry?>
) {
    val shareDisabled=remember { mutableStateOf(false) }
    SharedEvents.onShareIsDisabled= {
        shareDisabled.value = it
    }
    val printDisabled=remember { mutableStateOf(false) }
    SharedEvents.onPrintIsDisabled= {
        printDisabled.value = it
    }
    CenterAlignedTopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        navigationIcon = {
            when {
                showBackButton -> {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
                currentRoute.value?.destination?.route == "history" -> {
                    IconButton(onClick = { SharedEvents.openSideBar?.invoke() }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(R.string.more),
                        )
                    }
                }
            }
        },
        actions = {
            var menuExpanded by remember { mutableStateOf(false) }
            // Add settings icon visible when on history
            if (currentRoute.value?.destination?.route.equals("history")) {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                }
            }
            if (currentRoute.value?.destination?.route.equals("codeCreator")) {
                IconButton(onClick = {
                    if (!shareDisabled.value) {
                        shareDisabled.value = true
                        try {
                            SharedEvents.onShareClick?.invoke()
                        } finally {
                            shareDisabled.value = false
                        }
                    }
                }, enabled = !shareDisabled.value) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share))
                }
                IconButton(onClick = { menuExpanded = !menuExpanded }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.more),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.print)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_print_24),
                                contentDescription = stringResource(R.string.print)
                            )
                        },
                        onClick = {
                            SharedEvents.onPrintClick?.invoke()
                        }, enabled = !printDisabled.value)
                }
            }
        },
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
