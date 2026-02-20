package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
 * Display the content of a barcode that is a URL
 */
@Composable
fun UrlBarcodeDisplay(
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
) {
    val uriHandler = LocalUriHandler.current
    val saveBarcodeWithTagsUseCase: SaveBarcodeWithTagsUseCase = koinInject()
    val getOrCreateTagsByNameUseCase: GetOrCreateTagsByNameUseCase = koinInject()
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val saved = remember { mutableStateOf(false) }

    Title(title = stringResource(R.string.url))
    val noValue = stringResource(R.string.no_barcode_value)
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
            uriHandler.openUri(barcode.displayValue!!)
    })
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
    
    if (description != null || isLoadingDescription) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
    
    TextButton(onClick = {
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
            saveBarcodeWithTagsUseCase(barcodeModel, tags, aiGeneratedDescription)
        }
        saved.value = true
    }, enabled = !saved.value) {
        Text(text = if (!saved.value) stringResource(R.string.save) else stringResource(R.string.saved))
    }
}
