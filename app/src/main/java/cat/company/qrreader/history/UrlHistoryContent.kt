package cat.company.qrreader.history

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cat.company.qrreader.camera.bottomSheet.Title
import cat.company.qrreader.db.entities.SavedBarcode
import java.text.SimpleDateFormat

@Composable
fun UrlHistoryContent(sdf:SimpleDateFormat, barcode:SavedBarcode) {
    val uriHandler = LocalUriHandler.current
    Title(title = getTitle(barcode))
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
    if(barcode.description!=null&&barcode.description!!.trim()!="") {
        Spacer(modifier = Modifier.height(5.dp))
        Divider()
        Text(text = barcode.description!!)
    }
}