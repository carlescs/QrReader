package cat.company.qrreader.settings

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(context: Context) {
    val repo = SettingsRepository(context)
    val hideTaggedState by repo.hideTaggedWhenNoTagSelected.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ListItem(
            headlineContent = { Text(text = "Hide tagged elements when no tag selected") },
            supportingContent = { Text(text = "When enabled, elements that have any tag will be hidden if the user hasn't selected a tag in the history filter.") },
            trailingContent = {
                Switch(checked = hideTaggedState, onCheckedChange = { newValue ->
                    scope.launch { repo.setHideTaggedWhenNoTagSelected(newValue) }
                })
            },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
    }
}
