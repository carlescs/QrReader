package cat.company.qrreader.camera.bottomSheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
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

/**
 * Display the content of a barcode that is not a URL, email, phone, sms or contact

 */
@Composable
fun OtherContent(barcode: Barcode, db: BarcodesDb){
    val uriHandler = LocalUriHandler.current
    val coroutineScope= CoroutineScope(Dispatchers.IO)
    val saved = remember{ mutableStateOf(false) }
    Title(title = if (barcode.format==Barcode.FORMAT_EAN_13) "EAN13" else "Other")
    when (barcode.format) {
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E -> {
            Text(text = buildAnnotatedString {
                this.withStyle(
                    SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(barcode.displayValue ?: "No")
                }
            }, modifier = Modifier.clickable {
                if (barcode.displayValue != null)
                    uriHandler.openUri("https://www.google.com/search?q=${barcode.displayValue!!}&tbm=shop")
            })
        }
        else -> {
            Text(text = barcode.displayValue ?: "No")
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    TextButton(onClick = {
        coroutineScope.launch { db.savedBarcodeDao().insertAll(SavedBarcode(type = barcode.valueType, barcode = barcode.displayValue!!, format = barcode.format)) }
        saved.value=true
    }, enabled = !saved.value) {
        Text(text = if (!saved.value) "Save" else "Saved")
    }
}