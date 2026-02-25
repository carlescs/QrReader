package cat.company.qrreader.features.camera.presentation.ui.components

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.camera.AddTagToBarcodeUseCase
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeWithTagsUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Date

/**
 * Display the content of a WiFi barcode with a direct connect action.
 */
@Composable
fun WifiBarcodeDisplay(
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
    val context = LocalContext.current
    val connectivityManager = remember { context.getSystemService(ConnectivityManager::class.java) }
    var networkCallback by remember { mutableStateOf<ConnectivityManager.NetworkCallback?>(null) }

    val saveBarcodeWithTagsUseCase: SaveBarcodeWithTagsUseCase = koinInject()
    val getOrCreateTagsByNameUseCase: GetOrCreateTagsByNameUseCase = koinInject()
    val addTagToBarcodeUseCase: AddTagToBarcodeUseCase = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val saved = remember { mutableStateOf(false) }
    val savedBarcodeId = remember { mutableStateOf<Long?>(null) }
    val saveDescription = remember(description) { mutableStateOf(true) }

    val wifi = barcode.wifi
    val ssid = wifi?.ssid
    val password = wifi?.password
    val encryptionType = wifi?.encryptionType

    DisposableEffect(Unit) {
        onDispose {
            networkCallback?.let {
                try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            }
        }
    }

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
        onToggleTag = { tagName ->
            val isCurrentlySelected = suggestedTags.find { it.name == tagName }?.isSelected ?: false
            val willBeSelected = !isCurrentlySelected
            onToggleTag(tagName)
            if (willBeSelected) {
                coroutineScope.launch {
                    try {
                        val barcodeContent = barcode.rawValue ?: barcode.displayValue ?: return@launch
                        val tagColors = suggestedTags.associate { it.name to it.color }
                        val existingSavedId = savedBarcodeId.value
                        if (existingSavedId == null) {
                            val newSelectedNames = suggestedTags
                                .filter { if (it.name == tagName) true else it.isSelected }
                                .map { it.name }
                            val barcodeModel = BarcodeModel(
                                date = Date(),
                                type = barcode.valueType,
                                barcode = barcodeContent,
                                format = barcode.format
                            )
                            val tags = getOrCreateTagsByNameUseCase(newSelectedNames, tagColors)
                            val id = saveBarcodeWithTagsUseCase(barcodeModel, tags, if (saveDescription.value) aiGeneratedDescription else null)
                            savedBarcodeId.value = id
                            saved.value = true
                        } else {
                            val tags = getOrCreateTagsByNameUseCase(listOf(tagName), tagColors)
                            tags.firstOrNull()?.let { tag ->
                                addTagToBarcodeUseCase(existingSavedId.toInt(), tag.id)
                            }
                        }
                    } catch (_: Exception) {
                        // Fail silently; the user can still use the Save button
                    }
                }
            }
        },
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
            IconButton(onClick = {
                networkCallback?.let {
                    try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
                }
                val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(ssid)
                if (!password.isNullOrEmpty()) {
                    specifierBuilder.setWpa2Passphrase(password)
                }
                val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifierBuilder.build())
                    .build()
                val callback = object : ConnectivityManager.NetworkCallback() {}
                networkCallback = callback
                connectivityManager.requestNetwork(request, callback)
            }) {
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = stringResource(R.string.wifi_connect)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        val barcodeContent = barcode.rawValue ?: barcode.displayValue ?: return@launch
                        val barcodeModel = BarcodeModel(
                            date = Date(),
                            type = barcode.valueType,
                            barcode = barcodeContent,
                            format = barcode.format
                        )
                        val tags = if (selectedTagNames.isNotEmpty()) {
                            val tagColors = suggestedTags.associate { it.name to it.color }
                            getOrCreateTagsByNameUseCase(selectedTagNames, tagColors)
                        } else {
                            emptyList()
                        }
                        saveBarcodeWithTagsUseCase(barcodeModel, tags, if (saveDescription.value) aiGeneratedDescription else null).let { id ->
                            savedBarcodeId.value = id
                        }
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
