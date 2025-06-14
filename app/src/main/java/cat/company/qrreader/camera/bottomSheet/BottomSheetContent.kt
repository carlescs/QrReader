package cat.company.qrreader.camera.bottomSheet

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch

/**
 * Content of the bottom sheet
 */
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
        val clipboard:Clipboard= LocalClipboard.current
        val coroutineScope= rememberCoroutineScope()
        if (lastBarcode != null) {

            LazyColumn(modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
                items(
                    items = lastBarcode,
                    key={ it.hashCode() },
                    itemContent = { barcode ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    if (barcode.displayValue != null) {
                                        coroutineScope.launch {
                                            clipboard.setClipEntry(ClipEntry(
                                                ClipData.newPlainText(
                                                    "Barcode",
                                                    barcode.displayValue
                                                )
                                            ))
                                        }
                                    }
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Copied!") }
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
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
                                        OtherContent(barcode = barcode, db = db)
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }
}

