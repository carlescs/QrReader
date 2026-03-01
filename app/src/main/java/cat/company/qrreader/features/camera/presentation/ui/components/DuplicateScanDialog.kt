package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import java.text.DateFormat
import java.util.Date

/**
 * Dialog shown when a duplicate barcode is detected during scanning.
 *
 * Offers the user three choices via proper dialog button slots:
 * - Cancel (dismiss)
 * - Save again (secondary confirm)
 * - Open existing (primary confirm)
 *
 * @param existingDate The date of the previously saved duplicate entry.
 * @param onOpenExisting Called when the user chooses to view the existing entry.
 * @param onSaveAgain Called when the user chooses to save a new entry anyway.
 * @param onDismiss Called when the user cancels the dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateScanDialog(
    existingDate: Date,
    onOpenExisting: () -> Unit,
    onSaveAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedDate = remember(existingDate) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(existingDate)
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.duplicate_scan_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(R.string.duplicate_scan_message, formattedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    TextButton(onClick = onSaveAgain) {
                        Text(text = stringResource(R.string.duplicate_save_again))
                    }
                    TextButton(onClick = onOpenExisting) {
                        Text(text = stringResource(R.string.duplicate_open_existing))
                    }
                }
            }
        }
    }
}
