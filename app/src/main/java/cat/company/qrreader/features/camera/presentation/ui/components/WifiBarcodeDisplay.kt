package cat.company.qrreader.features.camera.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeWithTagsUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import cat.company.qrreader.ui.components.common.WifiConnectButton
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Date

/**
 * Display the content of a WiFi barcode with a direct connect action.
 *
 * @param barcode The scanned [Barcode] of type [Barcode.TYPE_WIFI].
 */
@Composable
fun WifiBarcodeDisplay(
    barcode: Barcode,
    snackbarHostState: SnackbarHostState,
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
    val rawContent = barcode.rawValue ?: barcode.displayValue ?: return
    WifiBarcodeDisplayContent(
        ssid = barcode.wifi?.ssid,
        password = barcode.wifi?.password,
        encryptionType = barcode.wifi?.encryptionType,
        rawContent = rawContent,
        barcodeFormat = barcode.format,
        snackbarHostState = snackbarHostState,
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
 * Internal implementation used by both the scanned-barcode and shared-WiFi-text paths.
 *
 * @param ssid The network SSID, or null if unknown.
 * @param password The network password, or null for open networks.
 * @param encryptionType The encryption type using [Barcode.WiFi] constants, or null if unknown.
 * @param rawContent The raw WIFI: string to persist when the user saves the barcode.
 * @param barcodeFormat The barcode format constant (defaults to [Barcode.FORMAT_QR_CODE]).
 */
@Composable
internal fun WifiBarcodeDisplayContent(
    ssid: String?,
    password: String?,
    encryptionType: Int?,
    rawContent: String,
    barcodeFormat: Int = Barcode.FORMAT_QR_CODE,
    snackbarHostState: SnackbarHostState,
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
    val saveBarcodeWithTagsUseCase: SaveBarcodeWithTagsUseCase = koinInject()
    val getOrCreateTagsByNameUseCase: GetOrCreateTagsByNameUseCase = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val saved = remember { mutableStateOf(false) }
    val saveDescription = remember(description) { mutableStateOf(true) }

    Title(title = stringResource(R.string.wifi))

    if (ssid != null) {
        Text(text = stringResource(R.string.wifi_ssid, ssid))
    }
    if (encryptionType != null) {
        val securityName = when (encryptionType) {
            Barcode.WiFi.TYPE_OPEN -> "Open"
            Barcode.WiFi.TYPE_WPA -> "WPA/WPA2"
            Barcode.WiFi.TYPE_WEP -> "WEP"
            else -> "Unknown"
        }
        Text(text = stringResource(R.string.wifi_security, securityName))
    }

    Spacer(modifier = Modifier.height(8.dp))

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
        if (ssid != null && encryptionType != Barcode.WiFi.TYPE_WEP) {
            WifiConnectButton(
                ssid = ssid,
                password = password,
                snackbarHostState = snackbarHostState
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        val barcodeModel = BarcodeModel(
                            date = Date(),
                            type = Barcode.TYPE_WIFI,
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
