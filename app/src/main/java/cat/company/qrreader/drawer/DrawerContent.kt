package cat.company.qrreader.drawer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    navController: NavHostController,
    coroutineScope: CoroutineScope,
    sidebarState: DrawerState
) {
    ModalDrawerSheet {
        Text(text = "QrReader", modifier = Modifier.padding(10.dp))
        Spacer(modifier = Modifier.height(12.dp))
        items.forEach {
            NavigationDrawerItem(
                icon = it.icon,
                label = { Text(text = it.label) },
                onClick = {
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute != null)
                        navController.navigate(it.route) {
                            popUpTo(currentRoute) {
                                saveState = true
                                inclusive = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    coroutineScope.launch { sidebarState.close() }
                },
                selected = navController.currentDestination?.route.equals(it.route),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }
}