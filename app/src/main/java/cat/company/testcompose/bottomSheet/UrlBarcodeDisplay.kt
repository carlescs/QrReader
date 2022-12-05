package cat.company.testcompose.bottomSheet

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.google.mlkit.vision.barcode.common.Barcode

@OptIn(ExperimentalUnitApi::class)
@Composable
fun UrlBarcodeDisplay(barcode: Barcode) {
    val uriHandler = LocalUriHandler.current
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
}