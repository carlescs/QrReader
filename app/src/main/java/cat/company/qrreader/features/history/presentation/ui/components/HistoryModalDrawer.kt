package cat.company.qrreader.features.history.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.features.settings.presentation.SettingsViewModel
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import cat.company.qrreader.features.tags.presentation.ui.components.AddTagDialog
import cat.company.qrreader.features.tags.presentation.ui.components.TagsFilterList
import cat.company.qrreader.ui.components.common.CountCircle
import cat.company.qrreader.utils.canAuthenticate
import cat.company.qrreader.utils.findFragmentActivity
import cat.company.qrreader.utils.showBiometricPrompt
import org.koin.androidx.compose.koinViewModel

/**
 * Content of the history modal drawer.
 *
 * Displays a favorites filter item, a safe section filter item, and a tag filter list,
 * allowing the user to:
 * - Toggle the favorites-only filter
 * - Open the safe section (requires biometric authentication) to view locked barcodes
 * - Select a tag to filter by (using a modern icon + name chip style)
 * - Add, edit, or delete tags
 * - Navigate to the Settings screen via a pinned item at the bottom
 *
 * @param selectedTagId Currently selected tag ID, or null if no tag is selected
 * @param showOnlyFavorites Whether the favorites filter is currently active
 * @param onToggleFavorites Callback invoked when the user toggles the favorites filter
 * @param showOnlySafe Whether the safe section filter is currently active
 * @param onToggleSafe Callback invoked when the user toggles the safe section filter.
 *                     Opening requires successful biometric authentication; closing requires none.
 * @param onNavigateToSettings Callback invoked when the user taps the Settings item
 * @param selectTag Callback invoked when the user selects or clears a tag filter
 * @param hideLocked Whether locked barcodes are hidden from the main history list
 * @param searchQuery Current search query text used to filter the main history list
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HistoryModalDrawerContent(
    selectedTagId: Int?,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    showOnlySafe: Boolean,
    onToggleSafe: () -> Unit,
    onNavigateToSettings: () -> Unit,
    selectTag: (TagModel?) -> Unit,
    hideLocked: Boolean = false,
    searchQuery: String = "",
    tagsViewModel: TagsViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val favoritesCount by tagsViewModel.favoritesCount.collectAsState(initial = 0)
    val lockedCount by tagsViewModel.lockedCount.collectAsState(initial = 0)
    val showTagCounters by settingsViewModel.showTagCounters.collectAsState(initial = true)
    val biometricLockEnabled by settingsViewModel.biometricLockEnabled.collectAsState(initial = false)
    val context = LocalContext.current
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
        ) {
            val dialogState = remember { mutableStateOf(false) }
            TopAppBar(title = { Text(text = stringResource(R.string.tags)) },
                actions = {
                    IconButton(onClick = {
                        dialogState.value = true
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.add_tag))
                    }
                    IconButton(onClick = { selectTag(null) }) {
                        Icon(imageVector = Icons.Filled.FilterListOff, contentDescription = stringResource(R.string.clear_filter))
                    }
                })
            NavigationDrawerItem(
                icon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null
                        )
                        if (showTagCounters && favoritesCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            CountCircle(
                                count = favoritesCount,
                                countDescription = stringResource(R.string.favorites_count_description, favoritesCount)
                            )
                        }
                    }
                },
                label = { Text(stringResource(R.string.favorites)) },
                selected = showOnlyFavorites,
                onClick = onToggleFavorites
            )
            if (biometricLockEnabled) {
                val canAuth = canAuthenticate(context)
                NavigationDrawerItem(
                    modifier = Modifier.alpha(if (canAuth) 1f else 0.38f),
                    icon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null
                            )
                            if (showTagCounters && lockedCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                CountCircle(
                                    count = lockedCount,
                                    countDescription = stringResource(R.string.locked_count_description, lockedCount)
                                )
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.safe_section)) },
                    selected = showOnlySafe,
                    onClick = {
                        if (showOnlySafe) {
                            onToggleSafe()
                        } else {
                            val activity = context.findFragmentActivity()
                            if (activity != null && canAuth) {
                                showBiometricPrompt(
                                    activity = activity,
                                    title = context.getString(R.string.unlock_safe_section),
                                    subtitle = context.getString(R.string.unlock_safe_section_subtitle),
                                    negativeButtonText = context.getString(R.string.cancel),
                                    onSuccess = { onToggleSafe() },
                                    onError = {}
                                )
                            }
                        }
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Box(modifier = Modifier.weight(1f)) {
                TagsFilterList(
                    viewModel = tagsViewModel,
                    selectedTagId = selectedTagId,
                    showTagCounters = showTagCounters,
                    showOnlyFavorites = showOnlyFavorites,
                    showOnlyLocked = showOnlySafe,
                    hideLocked = hideLocked,
                    searchQuery = searchQuery
                ) {
                    selectTag(it)
                }
            }
            if (dialogState.value) {
                AddTagDialog(tag = null) {
                    dialogState.value = false
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(R.string.settings)) },
                selected = false,
                onClick = onNavigateToSettings
            )
        }
    }
}

