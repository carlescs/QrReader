package cat.company.qrreader.features.history.presentation.ui.content

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.utils.parseContactVCard
import java.text.SimpleDateFormat

/**
 * Content for a saved contact barcode in the history view.
 * Shows the contact name, phone, and email.
 */
@Composable
fun ContactHistoryContent(sdf: SimpleDateFormat, barcode: BarcodeModel) {
    val contactInfo = remember(barcode.barcode) { parseContactVCard(barcode.barcode) }
    val hasContactFields = remember(contactInfo) {
        contactInfo.name != null ||
            contactInfo.phone != null ||
            contactInfo.email != null ||
            contactInfo.organization != null
    }

    Title(title = getTitle(barcode))
    Text(text = sdf.format(barcode.date))

    if (hasContactFields) {
        contactInfo.name?.let { Text(text = it) }
        contactInfo.phone?.let { Text(text = it) }
        contactInfo.email?.let { Text(text = it) }
    } else {
        Text(text = barcode.barcode)
    }

    if (barcode.description != null && barcode.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider()
        Text(text = barcode.description)
    }
}
