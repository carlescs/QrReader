package cat.company.qrreader.history.tags

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.entities.Tag
import cat.company.qrreader.history.DeleteConfirmDialog
import cat.company.qrreader.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TagCard(
    it: Tag,
    selectedTagId: Int?,
    selectTag: (Tag?) -> Unit,
    ioCoroutine: CoroutineScope,
    viewModel: TagsViewModel
) {
    val color = Utils.parseColor(it.color)
    Card(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = if (color != null) CardDefaults.cardColors(
            containerColor = color
        ) else CardDefaults.cardColors(),
        shape = RoundedCornerShape(16.dp),
        border = if (selectedTagId == it.id) BorderStroke(
            3.dp,
            if(isSystemInDarkTheme()) Color.White else Color.Black
        ) else null,
        onClick = {
            selectTag(it)
        }
    ) {
        TagCardContent(it, color, ioCoroutine, selectedTagId, selectTag, viewModel)
    }
}

@Composable
private fun TagCardContent(
    it: Tag,
    color: Color?,
    ioCoroutine: CoroutineScope,
    selectedTagId: Int?,
    selectTag: (Tag?) -> Unit,
    viewModel: TagsViewModel
) {
    val deleteDialogOpen = remember { mutableStateOf(false) }
    val editTag = remember { mutableStateOf<Tag?>(null) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = it.name,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            color = Utils.colorBasedOnBackground(color),
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { editTag.value = it }, modifier = Modifier.wrapContentSize()) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit tag",
                tint = Utils.colorBasedOnBackground(color)
            )
        }
        IconButton(onClick = {
            deleteDialogOpen.value = true
        }, modifier = Modifier.wrapContentSize()) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete tag",
                tint = Utils.colorBasedOnBackground(color)
            )
        }
    }
    if (deleteDialogOpen.value) {
        DeleteConfirmDialog(confirmDeleteOpen = deleteDialogOpen, item = it) {
            ioCoroutine.launch {
                if (selectedTagId == it.id) selectTag(null) // Clear filter
                viewModel.deleteTag(it)
            }
        }
    }
    if (editTag.value != null) {
        AddTagDialog(tag = editTag.value, db = viewModel.db, ioCoroutineScope = ioCoroutine) {
            editTag.value = null
        }
    }
}

