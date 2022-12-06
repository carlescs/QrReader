package cat.company.qrreader.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch

@Composable
fun DrawerItem(title:String,selected: Boolean,navController:NavController,drawerState: DrawerState,route:String) {
    val coroutineScope = rememberCoroutineScope()
    val background = if (selected) MaterialTheme.colors.primary else Color.Transparent
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(color = background)
            .clickable {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
                coroutineScope.launch { drawerState.close() }
            }) {
        Text(
            text = title,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}