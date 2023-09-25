package cat.company.qrreader.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.SavedBarcode
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun History(
    db: BarcodesDb,
    snackbarHostState: SnackbarHostState,
    viewModel: HistoryViewModel = HistoryViewModel(db = db)
) {
    viewModel.loadBarcodes()
    val state by viewModel.savedBarcodes.collectAsState(initial = emptyList())
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    if (state.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(text = "No saved barcodes!", modifier = Modifier.align(CenterHorizontally))
        }
    } else {
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = state) { barcode ->
                val editOpen=remember{ mutableStateOf(false) }
                val confirmDeleteOpen = remember{ mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(barcode.barcode))
                            coroutineScope.launch { snackbarHostState.showSnackbar("Copied!") }
                        },
                    shape = RoundedCornerShape(5.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        if(barcode.type==Barcode.TYPE_URL)
                            UrlHistoryContent(sdf = sdf, barcode = barcode)
                        else
                            OtherHistoryContent(sdf = sdf, barcode = barcode)

                        Spacer(modifier = Modifier.height(20.dp))
                        Row {
                            TextButton(onClick = {
                                confirmDeleteOpen.value = true
                            }) {
                                Text(text = "Delete")
                            }
                            TextButton(onClick = {
                                editOpen.value = true
                            }) {
                                Text(text = "Edit")
                            }
                        }
                        ShowEditBarcodeDialog(editOpen, barcode, db)
                        ShowDeleteConfirmationDialog(confirmDeleteOpen, coroutineScope, db, barcode)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowEditBarcodeDialog(
    editOpen: MutableState<Boolean>,
    barcode: SavedBarcode,
    db: BarcodesDb
) {
    if (editOpen.value) {
        EditBarcodeDialog(
            savedBarcode = barcode,
            onRequestClose = { editOpen.value = false },
            db = db
        )
    }
}

@Composable
private fun ShowDeleteConfirmationDialog(
    confirmDeleteOpen: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    db: BarcodesDb,
    barcode: SavedBarcode
) {
    if (confirmDeleteOpen.value) {
        AlertDialog(
            title = { Text(text = "Delete") },
            text = { Text(text = "Do you really want to delete this entry?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        db.savedBarcodeDao().delete(barcode)
                    }
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
}