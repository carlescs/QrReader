package cat.company.qrreader.features.history.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.features.tags.presentation.ui.components.AddTagDialog
import cat.company.qrreader.features.tags.presentation.ui.components.TagsFilterList

/**
 * Content of the history modal drawer.
 *
 * Displays a favorites filter item and a tag filter list, allowing the user to:
 * - Toggle the favorites-only filter
 * - Select a tag to filter by (using a modern icon + name chip style)
 * - Add, edit, or delete tags
 * - Navigate to the Settings screen via a pinned item at the bottom
 *
 * @param selectedTagId Currently selected tag ID, or null if no tag is selected
 * @param showOnlyFavorites Whether the favorites filter is currently active
 * @param onToggleFavorites Callback invoked when the user toggles the favorites filter
 * @param onNavigateToSettings Callback invoked when the user taps the Settings item
 * @param selectTag Callback invoked when the user selects or clears a tag filter
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HistoryModalDrawerContent(
    selectedTagId: Int?,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    selectTag: (TagModel?) -> Unit
) {
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
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_filter))
                    }
                })
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(R.string.favorites)) },
                selected = showOnlyFavorites,
                onClick = onToggleFavorites,
                badge = {
                    Switch(
                        checked = showOnlyFavorites,
                        onCheckedChange = { onToggleFavorites() }
                    )
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Box(modifier = Modifier.weight(1f)) {
                TagsFilterList(selectedTagId = selectedTagId) {
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

