package cat.company.qrreader.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.SavedBarcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun EditBarcodeDialog(savedBarcode:SavedBarcode, db: BarcodesDb, onRequestClose:()->Unit) {
    val coroutineScope= CoroutineScope(Dispatchers.IO)
    var text by remember { mutableStateOf(TextFieldValue(savedBarcode.title?:"")) }
    var description by remember { mutableStateOf(TextFieldValue(savedBarcode.description?:"")) }
    Dialog(
        onDismissRequest = { onRequestClose()},
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Edit barcode",
                        fontSize = 20.sp,
                        )
                    TextField(modifier = Modifier.padding(vertical = 5.dp), value = text, singleLine = true, onValueChange = {text=it}, label = { Text(
                        text = "Title"
                    )})
                    TextField(modifier = Modifier.padding(vertical = 5.dp), value = description, onValueChange = {description=it}, label = { Text(
                        text = "Description"
                    )})
                    Row(modifier = Modifier.align(Alignment.End)){
                        TextButton(onClick = {
                            onRequestClose()
                        }) {
                            Text(text = "Cancel")
                        }
                        TextButton(onClick = {
                            coroutineScope.launch {
                                db.runInTransaction {
                                    savedBarcode.title = text.text
                                    savedBarcode.description=description.text
                                    db.savedBarcodeDao().updateItem(savedBarcode)
                                }
                            }
                            onRequestClose()
                        }) {
                            Text(text = "Save")
                        }
                    }
                }
            }
        }
    }
}