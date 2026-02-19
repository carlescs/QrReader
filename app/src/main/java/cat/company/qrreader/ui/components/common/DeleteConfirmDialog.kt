package cat.company.qrreader.ui.components.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import cat.company.qrreader.R

/**
 * Dialog for confirming the deletion of an entry
 */
@Composable
fun <T> DeleteConfirmDialog(
    confirmDeleteOpen: MutableState<Boolean>,
    item: T,
    deleteFun: (T) -> Unit
) {
    AlertDialog(
        title = { Text(text = stringResource(R.string.delete)) },
        text = { Text(text = stringResource(R.string.delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = {
                deleteFun(item)
                confirmDeleteOpen.value = false
            }) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                confirmDeleteOpen.value = false
            }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { confirmDeleteOpen.value = false })
}

