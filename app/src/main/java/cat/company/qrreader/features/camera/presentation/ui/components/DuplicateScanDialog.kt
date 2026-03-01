package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cat.company.qrreader.R
import java.text.DateFormat
import java.util.Date

/**
 * Dialog shown when a duplicate barcode is detected during scanning.
 *
 * Offers the user three choices:
 * - Open the existing history entry (confirm button)
 * - Save the barcode again (secondary action in the dialog content)
 * - Cancel without saving (dismiss button)
 *
 * @param existingDate The date of the previously saved duplicate entry.
 * @param onOpenExisting Called when the user chooses to view the existing entry.
 * @param onSaveAgain Called when the user chooses to save a new entry anyway.
 * @param onDismiss Called when the user cancels the dialog.
 */
@Composable
fun DuplicateScanDialog(
    existingDate: Date,
    onOpenExisting: () -> Unit,
    onSaveAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(existingDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.duplicate_scan_title)) },
        text = {
            Column {
                Text(text = stringResource(R.string.duplicate_scan_message, formattedDate))
                TextButton(
                    onClick = onSaveAgain,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.duplicate_save_again))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenExisting) {
                Text(text = stringResource(R.string.duplicate_open_existing))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
