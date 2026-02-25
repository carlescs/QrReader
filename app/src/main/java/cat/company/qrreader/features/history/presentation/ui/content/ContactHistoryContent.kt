package cat.company.qrreader.features.history.presentation.ui.content

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.utils.parseContactVCard
import java.io.File
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.utils.ContactInfo
import java.text.SimpleDateFormat

/**
 * Content for a saved contact barcode in the history view.
 * Shows the contact name, phone, and email. Receives the pre-parsed [contactInfo] from
 * the parent to avoid redundant parsing.
 */
@Composable
fun ContactHistoryContent(sdf: SimpleDateFormat, barcode: BarcodeModel, contactInfo: ContactInfo?) {
    val hasContactFields = contactInfo != null && (
        contactInfo.name != null ||
            contactInfo.phone != null ||
            contactInfo.email != null ||
            contactInfo.organization != null
        )

    Title(title = getTitle(barcode))
    Text(text = sdf.format(barcode.date))

    if (hasContactFields) {
        contactInfo?.name?.let { Text(text = it) }
        contactInfo?.phone?.let { Text(text = it) }
        contactInfo?.email?.let { Text(text = it) }
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
        TextButton(onClick = {
            val vCardContent = if (barcode.barcode.trimStart().startsWith("BEGIN:VCARD", ignoreCase = true)) {
                barcode.barcode
            } else {
                buildString {
                    appendLine("BEGIN:VCARD")
                    appendLine("VERSION:3.0")
                    contactInfo.name?.let { appendLine("FN:$it") }
                    contactInfo.phone?.let { appendLine("TEL:$it") }
                    contactInfo.email?.let { appendLine("EMAIL:$it") }
                    contactInfo.organization?.let { appendLine("ORG:$it") }
                    append("END:VCARD")
                }
            }
            val contactsDir = File(context.cacheDir, "contacts")
            contactsDir.mkdirs()
            val vcfFile = File(contactsDir, "contact.vcf")
            vcfFile.writeText(vCardContent)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", vcfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/x-vcard"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }) {
            Text(text = stringResource(R.string.share))
        }
    }

    if (barcode.description != null && barcode.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider()
        Text(text = barcode.description)
    }
}
