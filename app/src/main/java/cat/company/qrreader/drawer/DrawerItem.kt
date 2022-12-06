package cat.company.qrreader.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.DrawerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch

@Composable
fun DrawerItem(title:String,navController:NavController,drawerState: DrawerState,route:String) {
    val coroutineScope= rememberCoroutineScope()
    Column(modifier = Modifier.clickable {
        navController.navigate(route){
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
        coroutineScope.launch{drawerState.close()}
    }) {
        Text(
            text = title,
            fontSize = 25.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Divider()
    }
}