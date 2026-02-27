package cat.company.qrreader.features.camera.presentation.ui.components

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.features.camera.presentation.BarcodeState
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch

/**
 * Content of the bottom sheet - displays scanned barcodes
 */
@Composable
fun BottomSheetContent(
    state: BarcodeState,
    snackbarHostState: SnackbarHostState,
    onToggleTag: (barcodeHash: Int, tagName: String) -> Unit
) {
    val lastBarcode = state.lastBarcode
    val sharedWifiInfo = state.sharedWifiInfo
    val sharedWifiRawText = state.sharedWifiRawText
    val sharedContactInfo = state.sharedContactInfo
    val sharedContactRawText = state.sharedContactRawText

    Column(
        modifier = Modifier
            .padding(15.dp)
            .defaultMinSize(minHeight = 250.dp)
    ) {
        val clipboard: Clipboard = LocalClipboard.current
        val coroutineScope = rememberCoroutineScope()

        if (sharedContactInfo != null && sharedContactRawText != null) {
            val contactHash = sharedContactRawText.hashCode()
            val suggestedTags = state.barcodeTags[contactHash] ?: emptyList()
            val isLoading = state.isLoadingTags.contains(contactHash)
            val error = state.tagSuggestionErrors[contactHash]
            val selectedTagNames = suggestedTags.filter { it.isSelected }.map { it.name }
            val aiDescription = state.barcodeDescriptions[contactHash]
            val isLoadingDescription = state.isLoadingDescriptions.contains(contactHash)
            val descriptionError = state.descriptionErrors[contactHash]

            LazyColumn(modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                    ) {
                        Column(modifier = Modifier.padding(15.dp)) {
                            ContactBarcodeDisplayContent(
                                contactInfo = sharedContactInfo,
                                rawContent = sharedContactRawText,
                                selectedTagNames = selectedTagNames,
                                aiGeneratedDescription = aiDescription,
                                aiGenerationEnabled = state.aiGenerationEnabled,
                                suggestedTags = suggestedTags,
                                isLoadingTags = isLoading,
                                tagError = error,
                                description = aiDescription,
                                isLoadingDescription = isLoadingDescription,
                                descriptionError = descriptionError,
                                onToggleTag = { tagName -> onToggleTag(contactHash, tagName) }
                            )
                        }
                    }
                }
            }
        } else if (sharedWifiInfo != null && sharedWifiRawText != null) {
            val wifiHash = sharedWifiRawText.hashCode()
            val suggestedTags = state.barcodeTags[wifiHash] ?: emptyList()
            val isLoading = state.isLoadingTags.contains(wifiHash)
            val error = state.tagSuggestionErrors[wifiHash]
            val selectedTagNames = suggestedTags.filter { it.isSelected }.map { it.name }
            val aiDescription = state.barcodeDescriptions[wifiHash]
            val isLoadingDescription = state.isLoadingDescriptions.contains(wifiHash)
            val descriptionError = state.descriptionErrors[wifiHash]
            val encryptionType = when (sharedWifiInfo.securityType?.uppercase()) {
                "WPA", "WPA2", "SAE" -> Barcode.WiFi.TYPE_WPA
                "WEP" -> Barcode.WiFi.TYPE_WEP
                else -> Barcode.WiFi.TYPE_OPEN
            }

            LazyColumn(modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                    ) {
                        Column(modifier = Modifier.padding(15.dp)) {
                            WifiBarcodeDisplayContent(
                                ssid = sharedWifiInfo.ssid,
                                password = sharedWifiInfo.password,
                                encryptionType = encryptionType,
                                rawContent = sharedWifiRawText,
                                snackbarHostState = snackbarHostState,
                                selectedTagNames = selectedTagNames,
                                aiGeneratedDescription = aiDescription,
                                aiGenerationEnabled = state.aiGenerationEnabled,
                                suggestedTags = suggestedTags,
                                isLoadingTags = isLoading,
                                tagError = error,
                                description = aiDescription,
                                isLoadingDescription = isLoadingDescription,
                                descriptionError = descriptionError,
                                onToggleTag = { tagName -> onToggleTag(wifiHash, tagName) }
                            )
                        }
                    }
                }
            }
        } else if (lastBarcode != null) {
            LazyColumn(modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp)) {
                items(
                    items = lastBarcode,
                    key = { it.hashCode() },
                    itemContent = { barcode ->
                        val barcodeHash = barcode.hashCode()
                        val copiedMsg = stringResource(R.string.copied)
                        
                        // Get tag data from the observed state (triggers recomposition when changed)
                        val suggestedTags = state.barcodeTags[barcodeHash] ?: emptyList()
                        val isLoading = state.isLoadingTags.contains(barcodeHash)
                        val error = state.tagSuggestionErrors[barcodeHash]
                        val selectedTagNames = suggestedTags.filter { it.isSelected }.map { it.name }
                        
                        // Get description data
                        val aiDescription = state.barcodeDescriptions[barcodeHash]
                        val isLoadingDescription = state.isLoadingDescriptions.contains(barcodeHash)
                        val descriptionError = state.descriptionErrors[barcodeHash]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    if (barcode.displayValue != null) {
                                        coroutineScope.launch {
                                            clipboard.setClipEntry(
                                                ClipEntry(
                                                    ClipData.newPlainText(
                                                        "Barcode",
                                                        barcode.displayValue
                                                    )
                                                )
                                            )
                                        }
                                    }
                                    coroutineScope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                        ) {
                            Column(modifier = Modifier.padding(15.dp)) {
                                when (barcode.valueType) {
                                    Barcode.TYPE_URL -> {
                                        UrlBarcodeDisplay(
                                            barcode = barcode,
                                            selectedTagNames = selectedTagNames,
                                            aiGeneratedDescription = aiDescription,
                                            aiGenerationEnabled = state.aiGenerationEnabled,
                                            suggestedTags = suggestedTags,
                                            isLoadingTags = isLoading,
                                            tagError = error,
                                            description = aiDescription,
                                            isLoadingDescription = isLoadingDescription,
                                            descriptionError = descriptionError,
                                            onToggleTag = { tagName ->
                                                onToggleTag(barcodeHash, tagName)
                                            }
                                        )
                                    }
                                    Barcode.TYPE_CONTACT_INFO -> {
                                        ContactBarcodeDisplay(
                                            barcode = barcode,
                                            selectedTagNames = selectedTagNames,
                                            aiGeneratedDescription = aiDescription,
                                            aiGenerationEnabled = state.aiGenerationEnabled,
                                            suggestedTags = suggestedTags,
                                            isLoadingTags = isLoading,
                                            tagError = error,
                                            description = aiDescription,
                                            isLoadingDescription = isLoadingDescription,
                                            descriptionError = descriptionError,
                                            onToggleTag = { tagName ->
                                                onToggleTag(barcodeHash, tagName)
                                            }
                                        )
                                    }
                                    Barcode.TYPE_WIFI -> {
                                        WifiBarcodeDisplay(
                                            barcode = barcode,
                                            snackbarHostState = snackbarHostState,
                                            selectedTagNames = selectedTagNames,
                                            aiGeneratedDescription = aiDescription,
                                            aiGenerationEnabled = state.aiGenerationEnabled,
                                            suggestedTags = suggestedTags,
                                            isLoadingTags = isLoading,
                                            tagError = error,
                                            description = aiDescription,
                                            isLoadingDescription = isLoadingDescription,
                                            descriptionError = descriptionError,
                                            onToggleTag = { tagName ->
                                                onToggleTag(barcodeHash, tagName)
                                            }
                                        )
                                    }
                                    else -> {
                                        OtherContent(
                                            barcode = barcode,
                                            selectedTagNames = selectedTagNames,
                                            aiGeneratedDescription = aiDescription,
                                            aiGenerationEnabled = state.aiGenerationEnabled,
                                            suggestedTags = suggestedTags,
                                            isLoadingTags = isLoading,
                                            tagError = error,
                                            description = aiDescription,
                                            isLoadingDescription = isLoadingDescription,
                                            descriptionError = descriptionError,
                                            onToggleTag = { tagName ->
                                                onToggleTag(barcodeHash, tagName)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }
}

