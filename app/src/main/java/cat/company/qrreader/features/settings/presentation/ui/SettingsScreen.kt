package cat.company.qrreader.features.settings.presentation.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.features.settings.presentation.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

private data class LanguageOption(val code: String, @StringRes val nameRes: Int)

private val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("en", R.string.language_english),
    LanguageOption("es", R.string.language_spanish),
    LanguageOption("fr", R.string.language_french),
    LanguageOption("de", R.string.language_german),
    LanguageOption("it", R.string.language_italian),
    LanguageOption("pt", R.string.language_portuguese),
    LanguageOption("zh", R.string.language_chinese),
    LanguageOption("ja", R.string.language_japanese),
    LanguageOption("ko", R.string.language_korean),
    LanguageOption("ar", R.string.language_arabic)
)

/**
 * Main settings screen showing navigation items for each settings section.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateToHistorySettings: () -> Unit = {},
    onNavigateToAiSettings: () -> Unit = {}
) {
    val isAiAvailableOnDevice by viewModel.isAiAvailableOnDevice.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SettingsNavigationItem(
            title = stringResource(R.string.history),
            subtitle = stringResource(R.string.history_settings_description),
            onClick = onNavigateToHistorySettings
        )
        HorizontalDivider()
        if (isAiAvailableOnDevice) {
            SettingsNavigationItem(
                title = stringResource(R.string.settings_section_ai),
                subtitle = stringResource(R.string.ai_settings_description),
                onClick = onNavigateToAiSettings
            )
            HorizontalDivider()
        }
    }
}

/**
 * History settings sub-screen showing history display preferences.
 */
@Composable
fun HistorySettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val hideTaggedState by viewModel.hideTaggedWhenNoTagSelected.collectAsState(initial = false)
    val searchAcrossAllState by viewModel.searchAcrossAllTagsWhenFiltering.collectAsState(initial = false)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
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
    }
}

/**
 * AI settings sub-screen showing AI feature configuration options.
 */
@Composable
fun AiSettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val aiGenerationState by viewModel.aiGenerationEnabled.collectAsState(initial = true)
    val aiLanguageState by viewModel.aiLanguage.collectAsState(initial = "en")
    val aiHumorousState by viewModel.aiHumorousDescriptions.collectAsState(initial = false)
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
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
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        val currentLanguageName = SUPPORTED_LANGUAGES.firstOrNull { it.code == aiLanguageState }
            ?.let { stringResource(it.nameRes) }
            ?: stringResource(R.string.language_english)
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.ai_language)) },
            supportingContent = { Text(text = stringResource(R.string.ai_language_description)) },
            trailingContent = {
                TextButton(onClick = { showLanguageDialog = true }) {
                    Text(text = currentLanguageName)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.ai_humorous_descriptions)) },
            supportingContent = { Text(text = stringResource(R.string.ai_humorous_descriptions_description)) },
            trailingContent = {
                Switch(checked = aiHumorousState, onCheckedChange = { newValue ->
                    viewModel.setAiHumorousDescriptions(newValue)
                })
            },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = aiLanguageState,
            onLanguageSelected = { code ->
                viewModel.setAiLanguage(code)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(text = subtitle) },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.ListItemDefaults.colors()
    )
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(text = stringResource(R.string.ai_language)) },
        text = {
            Column {
                SUPPORTED_LANGUAGES.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = option.code == currentLanguage,
                            onClick = { onLanguageSelected(option.code) }
                        )
                        Text(text = stringResource(option.nameRes))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        onDismissRequest = onDismiss
    )
}
