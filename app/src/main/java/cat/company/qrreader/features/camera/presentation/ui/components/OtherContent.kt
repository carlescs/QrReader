package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeWithTagsUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Date

/**
 * Display the content of a barcode that is not a URL, email, phone, sms or contact

 */
@Composable
fun OtherContent(
    barcode: Barcode,
    selectedTagNames: List<String> = emptyList(),
    aiGeneratedDescription: String? = null,
    aiGenerationEnabled: Boolean = true,
    suggestedTags: List<cat.company.qrreader.domain.model.SuggestedTagModel> = emptyList(),
    isLoadingTags: Boolean = false,
    tagError: String? = null,
    description: String? = null,
    isLoadingDescription: Boolean = false,
    descriptionError: String? = null,
    onToggleTag: (String) -> Unit = {}
){
    val uriHandler = LocalUriHandler.current
    val saveBarcodeWithTagsUseCase: SaveBarcodeWithTagsUseCase = koinInject()
    val getOrCreateTagsByNameUseCase: GetOrCreateTagsByNameUseCase = koinInject()
    val coroutineScope= CoroutineScope(Dispatchers.IO)
    val saved = remember{ mutableStateOf(false) }
    val saveDescription = remember(description) { mutableStateOf(true) }
    Title(title = if (barcode.format==Barcode.FORMAT_EAN_13) stringResource(R.string.ean13) else stringResource(R.string.other))
    val noValue = stringResource(R.string.no_barcode_value)
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
                    append(barcode.displayValue ?: noValue)
                }
            }, modifier = Modifier.clickable {
                if (barcode.displayValue != null)
                    uriHandler.openUri("https://www.google.com/search?q=${barcode.displayValue!!}&tbm=shop")
            })
        }
        else -> {
            Text(text = barcode.displayValue ?: noValue)
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    
    // Show suggested tags for THIS specific barcode
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
    
    // Show AI-generated description
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
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                coroutineScope.launch {
                    val barcodeModel = BarcodeModel(
                        date = Date(),
                        type = barcode.valueType,
                        barcode = barcode.displayValue!!,
                        format = barcode.format
                    )

                    // Get or create tags
                    val tags = if (selectedTagNames.isNotEmpty()) {
                        val tagColors = suggestedTags.associate { it.name to it.color }
                        getOrCreateTagsByNameUseCase(selectedTagNames, tagColors)
                    } else {
                        emptyList()
                    }

                    // Save barcode with tags
                    saveBarcodeWithTagsUseCase(barcodeModel, tags, if (saveDescription.value) aiGeneratedDescription else null)
                }
                saved.value = true
            },
            enabled = !saved.value
        ) {
            Icon(
                imageVector = if (saved.value) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (saved.value) stringResource(R.string.saved) else stringResource(R.string.save),
                tint = if (saved.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}