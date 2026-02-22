package cat.company.qrreader.features.camera.presentation.ui.components

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.domain.model.SuggestedTagModel
import cat.company.qrreader.domain.usecase.camera.SaveBarcodeWithTagsUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val saved = remember { mutableStateOf(false) }
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

    if (ssid != null && encryptionType != Barcode.WiFi.TYPE_WEP) {
        TextButton(onClick = {
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
            Text(text = stringResource(R.string.wifi_connect))
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

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

    TextButton(onClick = {
        coroutineScope.launch {
            val barcodeModel = BarcodeModel(
                date = Date(),
                type = barcode.valueType,
                barcode = barcode.displayValue!!,
                format = barcode.format
            )
            val tags = if (selectedTagNames.isNotEmpty()) {
                val tagColors = suggestedTags.associate { it.name to it.color }
                getOrCreateTagsByNameUseCase(selectedTagNames, tagColors)
            } else {
                emptyList()
            }
            saveBarcodeWithTagsUseCase(barcodeModel, tags, if (saveDescription.value) aiGeneratedDescription else null)
        }
        saved.value = true
    }, enabled = !saved.value) {
        Text(text = if (!saved.value) stringResource(R.string.save) else stringResource(R.string.saved))
    }
}
