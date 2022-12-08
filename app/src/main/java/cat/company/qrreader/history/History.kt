package cat.company.qrreader.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Card
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cat.company.qrreader.camera.bottomSheet.Title
import cat.company.qrreader.db.BarcodesDb
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
){
    viewModel.loadBarcodes()
    val state by viewModel.savedBarcodes.collectAsState(initial = emptyList())
    val coroutineScope= CoroutineScope(Dispatchers.IO)
    if(state.isEmpty()){
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(text = "No saved barcodes!", modifier = Modifier.align(CenterHorizontally))
        }
    }else {
        val clipboardManager:ClipboardManager= LocalClipboardManager.current
        val sdf=SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = state) { barcode ->
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
                        val uriHandler = LocalUriHandler.current
                        Title(title = "URL")
                        Text(text = sdf.format(barcode.date))
                        ClickableText(text = buildAnnotatedString {
                            this.withStyle(
                                SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(barcode.barcode)
                            }
                        }, onClick = {
                            uriHandler.openUri(barcode.barcode)
                        })
                        Spacer(modifier = Modifier.height(20.dp))
                        ClickableText(text = buildAnnotatedString {
                            this.withStyle(
                                SpanStyle(
                                    color = Color.Blue,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Delete")
                            }
                        }, onClick = {
                            coroutineScope.launch { db.savedBarcodeDao().delete(barcode) }
                        })
                    }
                }
            }
        }
    }
}