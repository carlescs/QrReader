package cat.company.qrreader.features.history.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.history.presentation.HistoryViewModel

/**
 * Dialog that displays the AI-generated description for a barcode,
 * with options to regenerate or remove the description.
 *
 * @param savedBarcode The barcode whose AI description is displayed.
 * @param viewModel The history ViewModel for regenerate/delete operations.
 * @param onDismiss Called when the dialog is dismissed.
 */
@Composable
fun AiDescriptionDialog(
    savedBarcode: BarcodeModel,
    viewModel: HistoryViewModel,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf(savedBarcode.aiGeneratedDescription ?: "") }
    var hasContent by remember { mutableStateOf(savedBarcode.aiGeneratedDescription != null) }
    // Track the latest saved barcode so regeneration always uses up-to-date data
    var currentBarcode by remember { mutableStateOf(savedBarcode) }
    val regenerateState by viewModel.regenerateDescriptionState.collectAsState()

    // When regeneration completes, save the new description to the database
    LaunchedEffect(regenerateState.description) {
        regenerateState.description?.let { newDescription ->
            val updatedBarcode = currentBarcode.copy(aiGeneratedDescription = newDescription)
            description = newDescription
            hasContent = true
            currentBarcode = updatedBarcode
            viewModel.updateBarcode(updatedBarcode)
            viewModel.resetRegenerateDescriptionState()
        }
    }

    // Reset regeneration state when dialog is closed
    DisposableEffect(Unit) {
        onDispose { viewModel.resetRegenerateDescriptionState() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(text = stringResource(R.string.ai_description)) },
        text = {
            Column {
                if (hasContent) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (!regenerateState.isLoading) {
                    Text(
                        text = stringResource(R.string.no_ai_description_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (regenerateState.error != null) {
                    Text(
                        text = regenerateState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (regenerateState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { viewModel.regenerateAiDescription(currentBarcode) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(
                                if (hasContent) R.string.regenerate_ai_description
                                else R.string.generate_ai_description
                            )
                        )
                    }
                    if (hasContent) {
                        IconButton(onClick = {
                            viewModel.updateBarcode(currentBarcode.copy(aiGeneratedDescription = null))
                            hasContent = false
                            onDismiss()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_ai_description)
                            )
                        }
                    }
                }
            }
        }
    )
}
