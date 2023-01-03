package cat.company.qrreader.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
                    elevation = 5.dp
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
                        if(editOpen.value){
                            EditBarcodeDialog(savedBarcode = barcode, onRequestClose = {editOpen.value=false},db=db)
                        }
                        if(confirmDeleteOpen.value) {
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
                }
            }
        }
    }
}