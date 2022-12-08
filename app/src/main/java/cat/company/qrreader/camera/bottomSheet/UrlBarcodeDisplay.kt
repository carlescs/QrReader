package cat.company.qrreader.camera.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.entities.SavedBarcode
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UrlBarcodeDisplay(barcode: Barcode,db:BarcodesDb) {
    val uriHandler = LocalUriHandler.current
    val coroutineScope= CoroutineScope(Dispatchers.IO)
    Title(title = "URL")
    ClickableText(text = buildAnnotatedString {
        this.withStyle(
            SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(barcode.displayValue ?: "No")
        }
    }, onClick = {
        if (barcode.displayValue != null)
            uriHandler.openUri(barcode.displayValue!!)
    })
    Spacer(modifier = Modifier.height(20.dp))
    ClickableText(text = AnnotatedString("Save")){
        coroutineScope.launch { db.savedBarcodeDao().insertAll(SavedBarcode(type = barcode.valueType, barcode = barcode.displayValue!!)) }
    }
}