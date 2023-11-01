package cat.company.qrreader.history

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun <T> DeleteConfirmDialog(
    confirmDeleteOpen: MutableState<Boolean>,
    item: T,
    deleteFun: (T) -> Unit
) {
    AlertDialog(
        title = { Text(text = "Delete") },
        text = { Text(text = "Do you really want to delete this entry?") },
        confirmButton = {
            TextButton(onClick = {
                deleteFun(item)
                confirmDeleteOpen.value = false
            }) {
                Text(text = "Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                confirmDeleteOpen.value = false
            }) {
                Text(text = "Cancel")
            }
        },
        onDismissRequest = { confirmDeleteOpen.value = false })
}