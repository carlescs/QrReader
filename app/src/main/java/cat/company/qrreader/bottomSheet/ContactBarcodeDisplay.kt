package cat.company.qrreader.bottomSheet

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun ContactBarcodeDisplay(barcode: Barcode) {
    Title(title = "Contact")
    Text(text = buildAnnotatedString {
        this.withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
            append("eMail: ")
        }
        append(barcode.contactInfo?.emails?.first()?.address!!)
    })
}