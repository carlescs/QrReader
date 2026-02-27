package cat.company.qrreader.features.camera.presentation.ui.components

import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeWithTagsUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import cat.company.qrreader.utils.ContactInfo
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Date

/**
 * Display the contact information from a barcode and offer an action to add it to the device contacts
 * or save it to history.
 */
@Composable
fun ContactBarcodeDisplay(
    barcode: Barcode,
    selectedTagNames: List<String> = emptyList(),
    aiGeneratedDescription: String? = null,
    aiGenerationEnabled: Boolean = true,
    suggestedTags: List<SuggestedTagModel> = emptyList(),
    isLoadingTags: Boolean = false,
    tagError: String? = null,
    description: String? = null,
    isLoadingDescription: Boolean = false,
    descriptionError: String? = null,
    onToggleTag: (String) -> Unit = {}
) {
    val name = barcode.contactInfo?.name
    val formattedName = name?.formattedName?.takeIf { it.isNotEmpty() }
        ?: listOfNotNull(name?.first, name?.middle, name?.last)
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    val phone = barcode.contactInfo?.phones?.firstOrNull()?.number
    val email = barcode.contactInfo?.emails?.firstOrNull()?.address
    val organization = barcode.contactInfo?.organization?.takeIf { it.isNotEmpty() }
    val rawContent = barcode.rawValue ?: barcode.displayValue ?: return

    ContactBarcodeDisplayContent(
        contactInfo = ContactInfo(
            name = formattedName.takeIf { it.isNotEmpty() },
            phone = phone,
            email = email,
            organization = organization
        ),
        rawContent = rawContent,
        barcodeFormat = barcode.format,
        selectedTagNames = selectedTagNames,
        aiGeneratedDescription = aiGeneratedDescription,
        aiGenerationEnabled = aiGenerationEnabled,
        suggestedTags = suggestedTags,
        isLoadingTags = isLoadingTags,
        tagError = tagError,
        description = description,
        isLoadingDescription = isLoadingDescription,
        descriptionError = descriptionError,
        onToggleTag = onToggleTag
    )
}

/**
 * Internal implementation used by both the scanned-barcode and shared-contact paths.
 *
 * @param contactInfo Parsed contact fields to display.
 * @param rawContent The raw vCard/MECARD string to persist when the user saves the barcode.
 * @param barcodeFormat The barcode format constant (defaults to [Barcode.FORMAT_QR_CODE]).
 */
@Composable
internal fun ContactBarcodeDisplayContent(
    contactInfo: ContactInfo,
    rawContent: String,
    barcodeFormat: Int = Barcode.FORMAT_QR_CODE,
    selectedTagNames: List<String> = emptyList(),
    aiGeneratedDescription: String? = null,
    aiGenerationEnabled: Boolean = true,
    suggestedTags: List<SuggestedTagModel> = emptyList(),
    isLoadingTags: Boolean = false,
    tagError: String? = null,
    description: String? = null,
    isLoadingDescription: Boolean = false,
    descriptionError: String? = null,
    onToggleTag: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val saveBarcodeWithTagsUseCase: SaveBarcodeWithTagsUseCase = koinInject()
    val getOrCreateTagsByNameUseCase: GetOrCreateTagsByNameUseCase = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val saved = remember { mutableStateOf(false) }
    val saveDescription = remember(description) { mutableStateOf(true) }

    Title(title = stringResource(R.string.contact))

    if (!contactInfo.name.isNullOrEmpty()) {
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(contactInfo.name) }
        })
    }

    contactInfo.phone?.let { phone ->
        Text(text = phone)
    }

    val emailLabel = stringResource(R.string.email_label)
    contactInfo.email?.let { email ->
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(emailLabel) }
            append(email)
        })
    }

    SuggestedTagsSection(
        suggestedTags = suggestedTags,
        isLoading = isLoadingTags,
        error = tagError,
        aiGenerationEnabled = aiGenerationEnabled,
        onToggleTag = onToggleTag,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )

    if (suggestedTags.isNotEmpty()) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }

    BarcodeDescriptionSection(
        description = description,
        isLoading = isLoadingDescription,
        error = descriptionError,
        aiGenerationEnabled = aiGenerationEnabled,
        saveDescription = saveDescription.value,
        onToggleSaveDescription = { saveDescription.value = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )

    if (description != null || isLoadingDescription) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                if (!contactInfo.name.isNullOrEmpty()) {
                    putExtra(ContactsContract.Intents.Insert.NAME, contactInfo.name)
                }
                contactInfo.phone?.let {
                    putExtra(ContactsContract.Intents.Insert.PHONE, it)
                }
                contactInfo.email?.let {
                    putExtra(ContactsContract.Intents.Insert.EMAIL, it)
                }
                contactInfo.organization?.let {
                    putExtra(ContactsContract.Intents.Insert.COMPANY, it)
                }
            }
            context.startActivity(intent)
        }) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = stringResource(R.string.add_to_contacts)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        val barcodeModel = BarcodeModel(
                            date = Date(),
                            type = Barcode.TYPE_CONTACT_INFO,
                            barcode = rawContent,
                            format = barcodeFormat
                        )
                        val tags = if (selectedTagNames.isNotEmpty()) {
                            val tagColors = suggestedTags.associate { it.name to it.color }
                            getOrCreateTagsByNameUseCase(selectedTagNames, tagColors)
                        } else {
                            emptyList()
                        }
                        saveBarcodeWithTagsUseCase(barcodeModel, tags, if (saveDescription.value) aiGeneratedDescription else null)
                        saved.value = true
                    } catch (_: Exception) {
                        // Keep saved as false so the user can retry if saving fails
                    }
                }
            },
            enabled = !saved.value
        ) {
            Icon(
                imageVector = if (saved.value) Icons.Filled.Save else Icons.Outlined.Save,
                contentDescription = if (saved.value) stringResource(R.string.saved) else stringResource(R.string.save),
                tint = if (saved.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}