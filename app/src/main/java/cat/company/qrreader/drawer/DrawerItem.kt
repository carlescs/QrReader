package cat.company.qrreader.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch

@Composable
fun DrawerItem(title:String,selected: Boolean,navController:NavController,drawerState: DrawerState,route:String) {
    val coroutineScope = rememberCoroutineScope()
    val background = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onPrimary
    val color = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onBackground
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
            color=color,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}