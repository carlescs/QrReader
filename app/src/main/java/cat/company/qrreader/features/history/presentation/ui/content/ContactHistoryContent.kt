package cat.company.qrreader.features.history.presentation.ui.content

import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.utils.parseContactVCard
import java.text.SimpleDateFormat

/**
 * Content for a saved contact barcode in the history view.
 * Shows the contact name, phone, and email, and offers an action to add it to the device contacts.
 */
@Composable
fun ContactHistoryContent(sdf: SimpleDateFormat, barcode: BarcodeModel) {
    val context = LocalContext.current
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

    if (hasContactFields) {
        TextButton(onClick = {
            val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                contactInfo.name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
                contactInfo.phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                contactInfo.email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
                contactInfo.organization?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
            }
            context.startActivity(intent)
        }) {
            Text(text = stringResource(R.string.add_to_contacts))
        }
    }

    if (barcode.description != null && barcode.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider()
        Text(text = barcode.description)
    }
}
