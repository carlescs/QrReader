package cat.company.qrreader.camera.bottomSheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch

@Composable
fun BottomSheetContent(
    lastBarcode: List<Barcode>?,
    db: BarcodesDb,
    snackbarHostState: SnackbarHostState
) {
    Column(
        modifier = Modifier
            .padding(15.dp)
            .defaultMinSize(minHeight = 250.dp)
    ) {
        val clipboardManager:ClipboardManager= LocalClipboardManager.current
        val coroutineScope= rememberCoroutineScope()
        if (lastBarcode != null) {
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(
                    items = lastBarcode,
                    key={ it.hashCode() },
                    itemContent = { barcode ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    if (barcode.displayValue != null)
                                        clipboardManager.setText(AnnotatedString(barcode.displayValue!!))
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Copied!") }
                                },
                            shape = RoundedCornerShape(5.dp),
                            elevation = 5.dp
                        ) {
                            Column(modifier = Modifier.padding(15.dp)) {
                                when (barcode.valueType) {
                                    Barcode.TYPE_URL -> {
                                        UrlBarcodeDisplay(barcode = barcode, db = db)
                                    }
                                    Barcode.TYPE_CONTACT_INFO -> {
                                        ContactBarcodeDisplay(barcode = barcode)
                                    }
                                    else -> {
                                        Title(title = "Other")
                                        Text(text = barcode.displayValue ?: "No")
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }
}

