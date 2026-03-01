package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import cat.company.qrreader.R
import java.text.DateFormat
import java.util.Date

/**
 * Dialog shown when a duplicate barcode is detected during scanning.
 *
 * Offers the user three choices:
 * - Cancel — dismisses the dialog ([dismissButton] slot)
 * - Save again — saves a new entry regardless ([dismissButton] slot)
 * - Open existing — navigates to the existing entry ([confirmButton] slot)
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
    val formattedDate = remember(existingDate.time) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(existingDate)
    }

    AlertDialog(
        title = { Text(text = stringResource(R.string.duplicate_scan_title)) },
        text = { Text(text = stringResource(R.string.duplicate_scan_message, formattedDate)) },
        confirmButton = {
            TextButton(onClick = onOpenExisting) {
                Text(text = stringResource(R.string.duplicate_open_existing))
            }
        },
        dismissButton = {
            FlowRow {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = onSaveAgain) {
                    Text(text = stringResource(R.string.duplicate_save_again))
                }
            }
        },
        onDismissRequest = onDismiss
    )
}
