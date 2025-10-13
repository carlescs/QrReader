package cat.company.qrreader.history

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.compound.SavedBarcodeWithTags
import cat.company.qrreader.history.content.OtherHistoryContent
import cat.company.qrreader.history.content.UrlHistoryContent
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

/**
 * Card for displaying a barcode
 */
@Composable
fun BarcodeCard(
    clipboardManager: Clipboard,
    barcode: SavedBarcodeWithTags,
    snackBarHostState: SnackbarHostState,
    sdf: SimpleDateFormat,
    db: BarcodesDb,
    viewModel: HistoryViewModel
) {
    val editOpen = remember { mutableStateOf(false) }
    val confirmDeleteOpen = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable {
                coroutineScope.launch {
                    clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("barcode", barcode.barcode.barcode)))
                    snackBarHostState.showSnackbar("Copied!")
                }
            },
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    if (barcode.barcode.type == Barcode.TYPE_URL)
                        UrlHistoryContent(sdf = sdf, barcode = barcode.barcode)
                    else
                        OtherHistoryContent(sdf = sdf, barcode = barcode.barcode)
                }
            }
        }

        Row(modifier = Modifier.padding(5.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val menuOpen = remember { mutableStateOf(false) }
            barcode.tags.forEach {
                Tag(it)
            }
            IconButton(onClick = { menuOpen.value = true }) {
                Icon(Icons.Filled.Add, contentDescription = "More")
            }
            DropdownMenu(
                expanded = menuOpen.value,
                onDismissRequest = { menuOpen.value = false }) {
                val tags = db.tagDao().getAll().collectAsState(initial = emptyList())
                tags.value.forEach {
                    DropdownMenuItem(text = { Text(text = it.name) },
                        leadingIcon = {
                            if(barcode.tags.contains(it))
                                Icon(imageVector = Icons.Filled.Check, contentDescription = "Check")
                        },
                        onClick = {
                            ioCoroutineScope.launch {
                                db.savedBarcodeDao().switchTag(barcode, it)
                                menuOpen.value = false
                            }
                        })
                }
            }
        }
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
        if (editOpen.value) {
            EditBarcodeDialog(
                savedBarcode = barcode.barcode,
                viewModel = viewModel,
                onRequestClose = { editOpen.value = false }
            )
        }
        if (confirmDeleteOpen.value) {
            DeleteConfirmDialog(
                confirmDeleteOpen,
                item = barcode.barcode
            ) {
                ioCoroutineScope.launch {
                    db.savedBarcodeDao().delete(it)
                }

            }
        }
    }
}
