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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import cat.company.qrreader.BuildConfig
import cat.company.qrreader.R
import cat.company.qrreader.domain.usecase.update.UpdateCheckResult
import cat.company.qrreader.features.settings.presentation.SettingsViewModel
import cat.company.qrreader.utils.canAuthenticate
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult as PlayActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private data class LanguageOption(val code: String, @StringRes val nameRes: Int)

private val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("device", R.string.language_device),
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
    appUpdateManager: AppUpdateManager = koinInject(),
    onNavigateToHistorySettings: () -> Unit = {},
    onNavigateToAiSettings: () -> Unit = {},
    onNavigateToSecuritySettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val isAiAvailableOnDevice by viewModel.isAiAvailableOnDevice.collectAsState()
    val updateCheckResult by viewModel.updateCheckResult.collectAsState()
    val isCheckingForUpdates by viewModel.isCheckingForUpdates.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == PlayActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
            viewModel.onUpdateFlowFailed(
                context.getString(R.string.update_check_failed)
            )
        }
    }

    // When an update is available, launch the Play Store in-app update flow immediately.
    LaunchedEffect(updateCheckResult) {
        val result = updateCheckResult
        if (result is UpdateCheckResult.UpdateAvailable) {
            try {
                if (!result.appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    viewModel.onUpdateFlowFailed(
                        context.getString(R.string.update_check_failed)
                    )
                    return@LaunchedEffect
                }
                val flowStarted = appUpdateManager.startUpdateFlowForResult(
                    result.appUpdateInfo,
                    launcher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
                if (flowStarted) {
                    viewModel.clearUpdateCheckResult()
                } else {
                    viewModel.onUpdateFlowFailed(
                        context.getString(R.string.update_check_failed)
                    )
                }
            } catch (e: Exception) {
                viewModel.onUpdateFlowFailed(
                    context.getString(R.string.update_check_failed)
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SettingsNavigationItem(
            title = stringResource(R.string.history),
            subtitle = stringResource(R.string.history_settings_description),
            onClick = onNavigateToHistorySettings
        )
        HorizontalDivider()
        SettingsNavigationItem(
            title = stringResource(R.string.settings_section_security),
            subtitle = stringResource(R.string.security_settings_description),
            onClick = onNavigateToSecuritySettings
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
        SettingsNavigationItem(
            title = stringResource(R.string.about),
            subtitle = stringResource(R.string.about_description),
            onClick = onNavigateToAbout
        )
        HorizontalDivider()
        AppVersionItem(
            isChecking = isCheckingForUpdates,
            onCheckForUpdates = { viewModel.checkForUpdates() }
        )
    }

    // Only show our own dialog for UpToDate and Error results.
    // UpdateAvailable is handled above by the Play Store in-app update sheet.
    updateCheckResult?.let { result ->
        if (result !is UpdateCheckResult.UpdateAvailable) {
            UpdateCheckResultDialog(
                result = result,
                onDismiss = { viewModel.clearUpdateCheckResult() }
            )
        }
    }
}

/**
 * About sub-screen showing app name, version, copyright and licence information.
 */
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME)) },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.about_copyright)) },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.about_licence)) },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
    }
}

/**
 * History settings sub-screen showing history display preferences.
 */
@Composable
fun HistorySettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val hideTaggedState by viewModel.hideTaggedWhenNoTagSelected.collectAsState(initial = false)
    val searchAcrossAllState by viewModel.searchAcrossAllTagsWhenFiltering.collectAsState(initial = false)
    val showTagCountersState by viewModel.showTagCounters.collectAsState(initial = true)
    val biometricLockState by viewModel.biometricLockEnabled.collectAsState(initial = false)
    val duplicateCheckState by viewModel.duplicateCheckEnabled.collectAsState(initial = true)
    val context = LocalContext.current
    val canUseBiometrics = remember { canAuthenticate(context) }

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
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.show_tag_counters)) },
            supportingContent = { Text(text = stringResource(R.string.show_tag_counters_description)) },
            trailingContent = {
                Switch(checked = showTagCountersState, onCheckedChange = { newValue ->
                    viewModel.setShowTagCounters(newValue)
                })
            },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
    }
}

/**
 * Security settings sub-screen showing security preferences including biometric lock.
 */
@Composable
fun SecuritySettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val biometricLockState by viewModel.biometricLockEnabled.collectAsState(initial = false)
    val duplicateCheckState by viewModel.duplicateCheckEnabled.collectAsState(initial = false)
    val context = LocalContext.current
    val canUseBiometrics = remember { canAuthenticate(context) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.biometric_lock_enabled)) },
            supportingContent = {
                Text(
                    text = stringResource(
                        if (canUseBiometrics) R.string.biometric_lock_description
                        else R.string.biometric_not_available
                    )
                )
            },
            trailingContent = {
                Switch(
                    checked = biometricLockState && canUseBiometrics,
                    onCheckedChange = { newValue -> viewModel.setBiometricLockEnabled(newValue) },
                    enabled = canUseBiometrics
                )
            },
            colors = androidx.compose.material3.ListItemDefaults.colors()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.duplicate_check_enabled)) },
            supportingContent = { Text(text = stringResource(R.string.duplicate_check_description)) },
            trailingContent = {
                Switch(checked = duplicateCheckState, onCheckedChange = { newValue ->
                    viewModel.setDuplicateCheckEnabled(newValue)
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
    val aiLanguageState by viewModel.aiLanguage.collectAsState(initial = "device")
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
        if (aiGenerationState) {
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
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ListItemDefaults.colors()
            )
        }
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

@Composable
private fun AppVersionItem(
    isChecking: Boolean,
    onCheckForUpdates: () -> Unit
) {
    val checkingLabel = stringResource(R.string.checking_for_updates)
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.app_version),
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = { Text(text = BuildConfig.VERSION_NAME) },
        trailingContent = {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                        .semantics { contentDescription = checkingLabel }
                )
            } else {
                TextButton(onClick = onCheckForUpdates) {
                    Text(text = stringResource(R.string.check_for_updates))
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.ListItemDefaults.colors()
    )
}

@Composable
private fun UpdateCheckResultDialog(
    result: UpdateCheckResult,
    onDismiss: () -> Unit
) {
    when (result) {
        is UpdateCheckResult.UpdateAvailable -> { /* handled by Play Store in-app update sheet */ }
        is UpdateCheckResult.UpToDate -> AlertDialog(
            title = { Text(text = stringResource(R.string.already_up_to_date)) },
            text = { Text(text = BuildConfig.VERSION_NAME) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            onDismissRequest = onDismiss
        )
        is UpdateCheckResult.Error -> AlertDialog(
            title = { Text(text = stringResource(R.string.update_check_failed)) },
            text = { Text(text = stringResource(R.string.update_check_failed_description)) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            onDismissRequest = onDismiss
        )
    }
}
