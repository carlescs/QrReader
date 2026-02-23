package cat.company.qrreader.features.camera.presentation.ui.components

import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import cat.company.qrreader.R
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Display the contact information from a barcode and offer an action to add it to the device contacts.
 */
@Composable
fun ContactBarcodeDisplay(barcode: Barcode) {
    val context = LocalContext.current
    Title(title = stringResource(R.string.contact))

    val name = barcode.contactInfo?.name
    val formattedName = name?.formattedName?.takeIf { it.isNotEmpty() }
        ?: listOfNotNull(name?.first, name?.middle, name?.last)
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    if (formattedName.isNotEmpty()) {
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(formattedName) }
        })
    }

    barcode.contactInfo?.phones?.firstOrNull()?.number?.let { phone ->
        Text(text = phone)
    }

    val emailLabel = stringResource(R.string.email_label)
    barcode.contactInfo?.emails?.firstOrNull()?.address?.let { email ->
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(emailLabel) }
            append(email)
        })
    }

    TextButton(onClick = {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            if (formattedName.isNotEmpty()) {
                putExtra(ContactsContract.Intents.Insert.NAME, formattedName)
            }
            barcode.contactInfo?.phones?.firstOrNull()?.number?.let {
                putExtra(ContactsContract.Intents.Insert.PHONE, it)
            }
            barcode.contactInfo?.emails?.firstOrNull()?.address?.let {
                putExtra(ContactsContract.Intents.Insert.EMAIL, it)
            }
            barcode.contactInfo?.organization?.takeIf { it.isNotEmpty() }?.let {
                putExtra(ContactsContract.Intents.Insert.COMPANY, it)
            }
        }
        context.startActivity(intent)
    }) {
        Text(text = stringResource(R.string.add_to_contacts))
    }
}