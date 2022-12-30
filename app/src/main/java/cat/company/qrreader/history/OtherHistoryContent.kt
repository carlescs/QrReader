package cat.company.qrreader.history

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import cat.company.qrreader.camera.bottomSheet.Title
import cat.company.qrreader.db.entities.SavedBarcode
import com.google.mlkit.vision.barcode.common.Barcode
import java.text.SimpleDateFormat

@Composable
fun OtherHistoryContent(sdf:SimpleDateFormat, barcode:SavedBarcode){
    val uriHandler = LocalUriHandler.current
    Title(title = getTitle(barcode))
    Text(text = sdf.format(barcode.date))
    when (barcode.format) {
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E -> {
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
                uriHandler.openUri("https://www.google.com/search?q=${barcode.barcode}&tbm=shop")
            })
        }
        else ->
            Text(text = barcode.barcode)
    }
    if(barcode.description!=null&&barcode.description!!.trim()!="") {
        Divider()
        Text(text = barcode.description!!)
    }
}

