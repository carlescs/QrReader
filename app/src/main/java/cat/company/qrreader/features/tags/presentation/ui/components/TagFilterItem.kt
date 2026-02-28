package cat.company.qrreader.features.tags.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import cat.company.qrreader.ui.components.common.DeleteConfirmDialog
import cat.company.qrreader.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Compact tag filter item for the history sidebar drawer.
 *
 * Displays a tag using Material3 [NavigationDrawerItem] with:
 * - Colored label icon (tag's color) as leading icon
 * - Tag name in standard (non-inverted) text color
 * - Selection highlight via the NavigationDrawerItem selected state
 * - Edit and delete action buttons in the badge slot
 *
 * Tapping the item selects/deselects it as the active tag filter.
 */
@Composable
fun TagFilterItem(
    tag: TagModel,
    isSelected: Boolean,
    onSelectTag: (TagModel?) -> Unit,
    ioCoroutine: CoroutineScope,
    viewModel: TagsViewModel,
    barcodeCount: Int = 0
) {
    val tagColor = Utils.parseColor(tag.color) ?: MaterialTheme.colorScheme.primary
    val deleteDialogOpen = remember { mutableStateOf(false) }
    val editTag = remember { mutableStateOf<TagModel?>(null) }

    NavigationDrawerItem(
        icon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Label,
                    contentDescription = tag.name,
                    tint = tagColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Badge { Text(barcodeCount.toString()) }
            }
        },
        label = { Text(tag.name) },
        selected = isSelected,
        onClick = {
            if (isSelected) onSelectTag(null) else onSelectTag(tag)
        },
        badge = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { editTag.value = tag },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit_tag),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { deleteDialogOpen.value = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_tag),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    )

    if (deleteDialogOpen.value) {
        DeleteConfirmDialog(confirmDeleteOpen = deleteDialogOpen, item = tag) {
            ioCoroutine.launch {
                if (isSelected) onSelectTag(null)
                viewModel.deleteTag(it)
            }
        }
    }
    if (editTag.value != null) {
        AddTagDialog(tag = editTag.value) {
            editTag.value = null
        }
    }
}
