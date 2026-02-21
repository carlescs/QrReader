package cat.company.qrreader.features.history.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.history.presentation.HistoryViewModel

/**
 * Dialog for editing a barcode
 */
@Composable
fun EditBarcodeDialog(
    savedBarcode: BarcodeModel,
    viewModel: HistoryViewModel,
    onRequestClose: () -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue(savedBarcode.title?:"")) }
    var description by remember { mutableStateOf(TextFieldValue(savedBarcode.description?:"")) }
    var aiDescription by remember { mutableStateOf(TextFieldValue(savedBarcode.aiGeneratedDescription?:"")) }
    // Tracks whether the AI description field currently holds AI-generated content
    var hasAiContent by remember { mutableStateOf(savedBarcode.aiGeneratedDescription != null) }

    val regenerateState by viewModel.regenerateDescriptionState.collectAsState()

    // Update the AI description field when regeneration completes successfully
    LaunchedEffect(regenerateState.description) {
        regenerateState.description?.let { newDescription ->
            aiDescription = TextFieldValue(newDescription)
            hasAiContent = true
        }
    }

    // Reset regeneration state when dialog is closed
    DisposableEffect(Unit) {
        onDispose { viewModel.resetRegenerateDescriptionState() }
    }

    Dialog(
        onDismissRequest = { onRequestClose()},
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = stringResource(R.string.edit_barcode),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(PaddingValues(bottom = 16.dp))
                        )
                    TextField(modifier = Modifier.padding(vertical = 5.dp), value = text, singleLine = true, onValueChange = {text=it}, label = { Text(text = stringResource(R.string.title_label)) })
                    TextField(modifier = Modifier.padding(vertical = 5.dp), value = description, onValueChange = {description=it}, label = { Text(text = stringResource(R.string.description_label)) })
                    AiDescriptionField(
                        aiDescription = aiDescription,
                        hasAiContent = hasAiContent,
                        isRegenerating = regenerateState.isLoading,
                        onValueChange = { aiDescription = it },
                        onRegenerate = { viewModel.regenerateAiDescription(savedBarcode) },
                        onDelete = {
                            aiDescription = TextFieldValue("")
                            hasAiContent = false
                        }
                    )
                    Row(modifier = Modifier.align(Alignment.End)){
                        TextButton(onClick = {
                            onRequestClose()
                        }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            val updatedBarcode = savedBarcode.copy(
                                title = text.text,
                                description = description.text,
                                aiGeneratedDescription = if (hasAiContent) aiDescription.text.takeIf { it.isNotEmpty() } else null
                            )
                            viewModel.updateBarcode(updatedBarcode)
                            onRequestClose()
                        }) {
                            Text(text = stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiDescriptionField(
    aiDescription: TextFieldValue,
    hasAiContent: Boolean,
    isRegenerating: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = aiDescription,
            onValueChange = onValueChange,
            label = { Text(text = stringResource(R.string.ai_description)) },
            enabled = hasAiContent && !isRegenerating
        )
        if (isRegenerating) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            IconButton(onClick = onRegenerate) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.regenerate_ai_description)
                )
            }
            if (hasAiContent) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_ai_description)
                    )
                }
            }
        }
    }
}

