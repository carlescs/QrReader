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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.ui.components.common.ExpandableText
import com.google.mlkit.vision.barcode.common.Barcode
import java.text.SimpleDateFormat

/**
 * Content for the other history
 */
@Composable
fun OtherHistoryContent(sdf:SimpleDateFormat, barcode:BarcodeModel, aiGenerationEnabled: Boolean = true){
    val uriHandler = LocalUriHandler.current
    Title(title = getTitle(barcode))
    Text(text = sdf.format(barcode.date))
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
                    append(barcode.barcode)
                }
            }, modifier = Modifier.clickable {
                uriHandler.openUri("https://www.google.com/search?q=${barcode.barcode}&tbm=shop")
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
    if(barcode.aiGeneratedDescription!=null && barcode.aiGeneratedDescription.trim()!="" && aiGenerationEnabled) {
        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider()
        ExpandableText(text = stringResource(R.string.ai_description_formatted, barcode.aiGeneratedDescription.orEmpty()))
    }
}

