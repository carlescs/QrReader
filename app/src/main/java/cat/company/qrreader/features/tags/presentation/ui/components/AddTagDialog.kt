package cat.company.qrreader.features.tags.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cat.company.qrreader.domain.model.TagModel
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import cat.company.qrreader.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Dialog for adding a new tag or editing an existing one
 */
@Composable
fun AddTagDialog(
    tag: TagModel? = null,
    viewModel: TagsViewModel = koinViewModel(),
    onRequestDismiss: () -> Unit
) {
    var tagName by remember { mutableStateOf(TextFieldValue(tag?.name?:"")) }
    var color by remember { mutableStateOf(tag?.color?:"") }
    var tagNameTouched by remember { mutableStateOf(false) }
    val colorDialogVisible=remember{ mutableStateOf(false) }
    val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    Dialog(onDismissRequest = { onRequestDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 10.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Add tag",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(PaddingValues(bottom = 16.dp))
                    )
                    TextField(
                        modifier = Modifier.padding(vertical = 5.dp),
                        value = tagName,
                        singleLine = true,
                        onValueChange = {
                            tagName = it
                            tagNameTouched = true
                        },
                        label = {
                            Text(
                                text = "Tag name"
                            )
                        },
                        isError = tagName.text.isBlank() && tagNameTouched
                    )
                    TextField(
                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .clickable {
                                colorDialogVisible.value = true
                            },
                        value = color,
                        onValueChange = { color = it },
                        colors = if (color.isNotBlank()) TextFieldDefaults.colors(
                            disabledContainerColor = Utils.parseColor(color)!!,
                        ) else TextFieldDefaults.colors(),
                        label = {
                            Text(
                                text = "Color"
                            )
                        },
                        enabled = false,
                        singleLine = true
                    )
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButton(onClick = {
                            onRequestDismiss()
                        }) {
                            Text(text = "Cancel")
                        }
                        TextButton(onClick = {
                            saveTag(tag, ioCoroutineScope, viewModel, tagName, color)
                            onRequestDismiss()
                        }, enabled = tagName.text.isNotBlank()) {
                            Text(text = "Save")
                        }
                    }
                }
                if(colorDialogVisible.value){
                    ColorPickerDialog(colorDialogVisible, color){
                        color=it
                        colorDialogVisible.value=false
                    }
                }
            }
        }
    }
}

/**
 * Save tag to the database
 */
private fun saveTag(
    tag: TagModel?,
    ioCoroutineScope: CoroutineScope,
    viewModel: TagsViewModel,
    tagName: TextFieldValue,
    color: String
) {
    if (tag == null)
        ioCoroutineScope.launch {
            viewModel.insertTags(TagModel(name = tagName.text, color = color))
        }
    else
        ioCoroutineScope.launch {
            viewModel.updateTag(TagModel(id = tag.id, name = tagName.text, color = color))
        }
}

