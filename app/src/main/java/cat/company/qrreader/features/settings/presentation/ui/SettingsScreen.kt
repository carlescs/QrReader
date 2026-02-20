package cat.company.qrreader.features.settings.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.features.settings.presentation.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val hideTaggedState by viewModel.hideTaggedWhenNoTagSelected.collectAsState(initial = false)
    val searchAcrossAllState by viewModel.searchAcrossAllTagsWhenFiltering.collectAsState(initial = false)
    val aiGenerationState by viewModel.aiGenerationEnabled.collectAsState(initial = true)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.hide_tagged_when_no_tag_selected)) },
            supportingContent = { Text(text = stringResource(R.string.hide_tagged_description)) },
            trailingContent = {
                Switch(checked = hideTaggedState, onCheckedChange = { newValue ->
                    viewModel.setHideTaggedWhenNoTagSelected(newValue)
                })
            },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.search_across_all_tags)) },
            supportingContent = { Text(text = stringResource(R.string.search_across_all_tags_description)) },
            trailingContent = {
                Switch(checked = searchAcrossAllState, onCheckedChange = { newValue ->
                    viewModel.setSearchAcrossAllTagsWhenFiltering(newValue)
                })
            },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.ai_features)) },
            supportingContent = { Text(text = stringResource(R.string.ai_features_description)) },
            trailingContent = {
                Switch(checked = aiGenerationState, onCheckedChange = { newValue ->
                    viewModel.setAiGenerationEnabled(newValue)
                })
            },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
    }
}
