package cat.company.qrreader.ui.components.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cat.company.qrreader.R

/**
 * Bottom navigation icon
 */
class BottomIcon(
    val icon: @Composable () -> Unit,
    @StringRes val labelRes: Int,
    val route: String
)

val items = arrayListOf(
    BottomIcon(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_history_24),
                contentDescription = stringResource(R.string.history)
            )
        },
        labelRes = R.string.history,
        route = "history"
    ),
    BottomIcon(
        icon = {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = stringResource(R.string.code_creator)
            )
        },
        labelRes = R.string.code_creator,
        route = "codeCreator"
    ),
)

