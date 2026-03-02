package cat.company.qrreader.features.history.presentation.ui.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
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
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.features.history.presentation.ui.components.isIsbn
import com.google.mlkit.vision.barcode.common.Barcode
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 * Content for the other history
 */
@Composable
fun OtherHistoryContent(sdf:SimpleDateFormat, barcode:BarcodeModel){
    val uriHandler = LocalUriHandler.current
    Title(title = getTitle(barcode))
    Text(text = sdf.format(barcode.date))
    when {
        isIsbn(barcode) -> {
            Text(text = buildAnnotatedString {
                this.withStyle(
                    SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(barcode.barcode)
                }
            }, modifier = Modifier.clickable {
                uriHandler.openUri("https://openlibrary.org/isbn/${URLEncoder.encode(barcode.barcode.trim(), "UTF-8")}")
            })
        }
        barcode.format == Barcode.FORMAT_EAN_13 ||
        barcode.format == Barcode.FORMAT_EAN_8 ||
        barcode.format == Barcode.FORMAT_UPC_A ||
        barcode.format == Barcode.FORMAT_UPC_E -> {
            Text(text = buildAnnotatedString {
                this.withStyle(
                    SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(barcode.barcode)
                }
            }, modifier = Modifier.clickable {
                uriHandler.openUri("https://www.google.com/search?q=${URLEncoder.encode(barcode.barcode, "UTF-8")}&tbm=shop")
            })
        }
        else ->
            Text(text = barcode.barcode)
    }
    if(barcode.description!=null&& barcode.description.trim()!="") {
        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider()
        Text(text = barcode.description)
    }
}

