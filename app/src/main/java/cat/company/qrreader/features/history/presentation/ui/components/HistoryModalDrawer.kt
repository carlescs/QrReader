package cat.company.qrreader.features.history.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.features.tags.presentation.ui.components.AddTagDialog
import cat.company.qrreader.features.tags.presentation.ui.components.TagsList

/**
 * Content of the history modal drawer
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HistoryModalDrawerContent(selectedTagId:Int?, selectTag: (TagModel?) -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight()
                .wrapContentWidth()
        ) {
            val dialogState=remember{ mutableStateOf(false) }
            TopAppBar(title = { Text(text = "Tags") },
                actions = {
                    IconButton(onClick = {
                        dialogState.value=true
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add tag")
                    }
                    IconButton(onClick = { selectTag(null) }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear filter")
                    }
                })
            TagsList(selectedTagId= selectedTagId){
                selectTag(it)
            }
            if(dialogState.value) {
                AddTagDialog(tag = null) {
                    dialogState.value = false
                }
            }
        }
    }
}

