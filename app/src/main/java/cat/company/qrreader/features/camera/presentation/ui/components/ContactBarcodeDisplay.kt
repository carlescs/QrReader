package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import cat.company.qrreader.R
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Display the contact information from a barcode
 */
@Composable
fun ContactBarcodeDisplay(barcode: Barcode) {
    Title(title = stringResource(R.string.contact))
    val emailLabel = stringResource(R.string.email_label)
    Text(text = buildAnnotatedString {
        this.withStyle(SpanStyle(fontWeight = FontWeight.Bold)){
            append(emailLabel)
        }
        append(barcode.contactInfo?.emails?.first()?.address!!)
    })
}