package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cat.company.qrreader.R
import java.text.DateFormat
import java.util.Date

/**
 * Dialog shown when a duplicate barcode is detected during scanning.
 *
 * Offers the user three choices:
 * - Open the existing history entry
 * - Save the barcode again (creating a new entry)
 * - Cancel without saving
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
        text = { Text(text = stringResource(R.string.duplicate_scan_message, formattedDate)) },
        confirmButton = {
            TextButton(onClick = onOpenExisting) {
                Text(text = stringResource(R.string.duplicate_open_existing))
            }
        },
        dismissButton = {
            TextButton(onClick = onSaveAgain) {
                Text(text = stringResource(R.string.duplicate_save_again))
            }
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
