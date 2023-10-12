package cat.company.qrreader.drawer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import cat.company.qrreader.R

class BottomIcon(
    val icon: @Composable () -> Unit,
    val label: String,
    val route: String
)

val items = arrayListOf(
    BottomIcon(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_history_24),
                contentDescription = "History"
            )
        },
        label = "History",
        route = "history"
    ),
    BottomIcon(
        icon = {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = "Code creator"
            )
        },
        label = "Code Creator",
        route = "codeCreator"
    ),
)