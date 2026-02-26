package cat.company.qrreader.features.history.presentation.ui.components

import android.content.ClipData
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeWithTagsModel
import cat.company.qrreader.features.history.presentation.HistoryViewModel
import cat.company.qrreader.features.history.presentation.ui.content.ContactHistoryContent
import cat.company.qrreader.features.history.presentation.ui.content.OtherHistoryContent
import cat.company.qrreader.features.history.presentation.ui.content.UrlHistoryContent
import cat.company.qrreader.features.history.presentation.ui.content.WifiHistoryContent
import cat.company.qrreader.features.tags.presentation.TagsViewModel
import cat.company.qrreader.ui.components.common.DeleteConfirmDialog
import cat.company.qrreader.ui.components.common.SelectableTag
import cat.company.qrreader.ui.components.common.Tag
import cat.company.qrreader.domain.usecase.history.SwitchBarcodeTagUseCase
import cat.company.qrreader.domain.usecase.tags.GetOrCreateTagsByNameUseCase
import cat.company.qrreader.utils.parseContactVCard
import cat.company.qrreader.utils.parseWifiContent
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat

/**
 * Card for displaying a barcode
 */
@Composable
fun BarcodeCard(
    clipboardManager: Clipboard,
    barcode: BarcodeWithTagsModel,
    snackBarHostState: SnackbarHostState,
    sdf: SimpleDateFormat,
    historyViewModel: HistoryViewModel,
    tagsViewModel: TagsViewModel = koinViewModel(),
    switchBarcodeTagUseCase: SwitchBarcodeTagUseCase = koinInject(),
    getOrCreateTagsByNameUseCase: GetOrCreateTagsByNameUseCase = koinInject()
) {
    val editOpen = remember { mutableStateOf(false) }
    val confirmDeleteOpen = remember { mutableStateOf(false) }
    val tagEditOpen = remember { mutableStateOf(false) }
    val aiDescriptionOpen = remember { mutableStateOf(false) }
    val moreMenuExpanded = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    val context = LocalContext.current
    val copiedMsg = stringResource(R.string.copied)
    val aiGenerationEnabled by historyViewModel.aiGenerationEnabled.collectAsState()
    val allTags by tagsViewModel.tags.collectAsState(initial = emptyList())
    val tagSuggestionStates by historyViewModel.tagSuggestionStates.collectAsState()
    val tagSuggestionState = tagSuggestionStates[barcode.barcode.id]
    val contactInfo = remember(barcode.barcode.barcode) {
        if (barcode.barcode.type == Barcode.TYPE_CONTACT_INFO) parseContactVCard(barcode.barcode.barcode)
        else null
    }
    val hasContactFields = remember(contactInfo) {
        contactInfo != null && (
            contactInfo.name != null ||
                contactInfo.phone != null ||
                contactInfo.email != null ||
                contactInfo.organization != null
            )
    }
    val wifiInfo = remember(barcode.barcode.barcode) {
        if (barcode.barcode.type == Barcode.TYPE_WIFI) parseWifiContent(barcode.barcode.barcode)
        else null
    }
    val isWifiWep = wifiInfo?.securityType?.uppercase() == "WEP"
    val connectivityManager = remember { context.getSystemService(ConnectivityManager::class.java) }
    val wifiNetworkCallback = remember { mutableStateOf<ConnectivityManager.NetworkCallback?>(null) }
    val wifiNetworkCallbackWpa3 = remember { mutableStateOf<ConnectivityManager.NetworkCallback?>(null) }
    val showWifiQrCodeDialog = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            wifiNetworkCallback.value?.let {
                try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            }
            wifiNetworkCallbackWpa3.value?.let {
                try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            }
        }
    }

    // Load tags
    tagsViewModel.loadTags()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable {
                coroutineScope.launch {
                    clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("barcode", barcode.barcode.barcode)))
                    snackBarHostState.showSnackbar(copiedMsg)
                }
            },
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = getBarcodeIcon(barcode.barcode.type),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (barcode.barcode.type == Barcode.TYPE_URL)
                        UrlHistoryContent(sdf = sdf, barcode = barcode.barcode)
                    else if (barcode.barcode.type == Barcode.TYPE_WIFI)
                        WifiHistoryContent(sdf = sdf, barcode = barcode.barcode)
                    else if (barcode.barcode.type == Barcode.TYPE_CONTACT_INFO)
                        ContactHistoryContent(sdf = sdf, barcode = barcode.barcode, contactInfo = contactInfo)
                    else
                        OtherHistoryContent(sdf = sdf, barcode = barcode.barcode)
                }
            }
        }

        // Tag section: collapsed shows only assigned tags; expanded shows all tags as FilterChips
        if (tagEditOpen.value && allTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allTags.forEach { tag ->
                    val isSelected = barcode.tags.contains(tag)
                    SelectableTag(
                        name = tag.name,
                        isSelected = isSelected,
                        onClick = {
                            ioCoroutineScope.launch {
                                switchBarcodeTagUseCase.invoke(barcode, tag)
                            }
                        }
                    )
                }
            }
            if (aiGenerationEnabled) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            historyViewModel.suggestTags(barcode, allTags.map { it.name })
                        },
                        enabled = tagSuggestionState?.isLoading != true
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.suggest_tags))
                    }
                    if (tagSuggestionState?.isLoading == true) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                val tagSuggestionError = tagSuggestionState?.error
                if (tagSuggestionError != null) {
                    val errorMessage = when {
                        tagSuggestionError.contains("not available on this device", ignoreCase = true) ||
                        tagSuggestionError.contains("UnsupportedOperation", ignoreCase = true) ->
                            stringResource(R.string.ai_suggestions_not_supported)
                        tagSuggestionError.contains("downloading", ignoreCase = true) ||
                        tagSuggestionError.contains("download", ignoreCase = true) ->
                            stringResource(R.string.ai_model_downloading_retry)
                        else -> stringResource(R.string.tag_suggestions_unavailable)
                    }
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
                val suggestedTags = tagSuggestionState?.suggestedTags
                if (!suggestedTags.isNullOrEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        suggestedTags.forEach { suggested ->
                            SelectableTag(
                                name = suggested.name,
                                isSelected = suggested.isSelected,
                                onClick = {
                                    ioCoroutineScope.launch {
                                        val existingTag = allTags.find {
                                            it.name.equals(suggested.name, ignoreCase = true)
                                        }
                                        if (existingTag != null) {
                                            switchBarcodeTagUseCase.invoke(barcode, existingTag)
                                        } else {
                                            val created = getOrCreateTagsByNameUseCase(
                                                listOf(suggested.name),
                                                mapOf(suggested.name to suggested.color)
                                            )
                                            created.firstOrNull()?.let {
                                                switchBarcodeTagUseCase.invoke(barcode, it)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.tap_to_add_suggested_tags),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }
        } else if (barcode.tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                barcode.tags.forEach { Tag(it) }
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { moreMenuExpanded.value = !moreMenuExpanded.value }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.more)
                )
                DropdownMenu(
                    expanded = moreMenuExpanded.value,
                    onDismissRequest = { moreMenuExpanded.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_barcode)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            moreMenuExpanded.value = false
                            editOpen.value = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_barcode)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            moreMenuExpanded.value = false
                            confirmDeleteOpen.value = true
                        }
                    )
                }
            }
            if (aiGenerationEnabled && !barcode.barcode.aiGeneratedDescription.isNullOrBlank()) {
                IconButton(onClick = { aiDescriptionOpen.value = true }) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = stringResource(R.string.ai_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (allTags.isNotEmpty()) {
                IconButton(onClick = {
                    if (tagEditOpen.value) {
                        historyViewModel.resetTagSuggestionState(barcode.barcode.id)
                    }
                    tagEditOpen.value = !tagEditOpen.value
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = stringResource(R.string.manage_tags),
                        tint = if (barcode.tags.isNotEmpty() || tagEditOpen.value)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { shareBarcode(context, barcode.barcode) }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.share)
                )
            }
            val wifiSsid = wifiInfo?.ssid
            if (barcode.barcode.type == Barcode.TYPE_WIFI && wifiSsid != null && !isWifiWep) {
                IconButton(onClick = {
                    wifiNetworkCallback.value?.let {
                        try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
                    }
                    wifiNetworkCallback.value = null
                    wifiNetworkCallbackWpa3.value?.let {
                        try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
                    }
                    wifiNetworkCallbackWpa3.value = null
                    val wifiPassword = wifiInfo?.password
                    try {
                        val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(wifiSsid)
                        if (!wifiPassword.isNullOrEmpty()) {
                            specifierBuilder.setWpa2Passphrase(wifiPassword)
                        }
                        val request = NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .setNetworkSpecifier(specifierBuilder.build())
                            .build()
                        val wpa2Callback = object : ConnectivityManager.NetworkCallback() {}
                        wifiNetworkCallback.value = wpa2Callback
                        connectivityManager.requestNetwork(request, wpa2Callback)
                    } catch (_: Exception) {}
                    if (!wifiPassword.isNullOrEmpty()) {
                        try {
                            val wpa3Specifier = WifiNetworkSpecifier.Builder()
                                .setSsid(wifiSsid)
                                .setWpa3Passphrase(wifiPassword)
                                .build()
                            val wpa3Request = NetworkRequest.Builder()
                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                .setNetworkSpecifier(wpa3Specifier)
                                .build()
                            val wpa3Callback = object : ConnectivityManager.NetworkCallback() {}
                            wifiNetworkCallbackWpa3.value = wpa3Callback
                            connectivityManager.requestNetwork(wpa3Request, wpa3Callback)
                        } catch (_: Exception) {}
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = stringResource(R.string.wifi_connect)
                    )
                }
            }
            if (barcode.barcode.type == Barcode.TYPE_WIFI) {
                IconButton(onClick = { showWifiQrCodeDialog.value = true }) {
                    Icon(
                        imageVector = Icons.Filled.QrCode,
                        contentDescription = stringResource(R.string.wifi_show_qr_code)
                    )
                }
            }
            if (hasContactFields) {
                IconButton(onClick = {
                    val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                        type = ContactsContract.RawContacts.CONTENT_TYPE
                        contactInfo?.name?.let { putExtra(ContactsContract.Intents.Insert.NAME, it) }
                        contactInfo?.phone?.let { putExtra(ContactsContract.Intents.Insert.PHONE, it) }
                        contactInfo?.email?.let { putExtra(ContactsContract.Intents.Insert.EMAIL, it) }
                        contactInfo?.organization?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
                    }
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = stringResource(R.string.add_to_contacts)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { historyViewModel.toggleFavorite(barcode.barcode.id, !barcode.barcode.isFavorite) }) {
                Icon(
                    imageVector = if (barcode.barcode.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(
                        if (barcode.barcode.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
                    ),
                    tint = if (barcode.barcode.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (editOpen.value) {
            EditBarcodeDialog(
                savedBarcode = barcode.barcode,
                viewModel = historyViewModel,
                onRequestClose = { editOpen.value = false }
            )
        }
        if (confirmDeleteOpen.value) {
            DeleteConfirmDialog(
                confirmDeleteOpen,
                item = barcode.barcode
            ) {
                ioCoroutineScope.launch {
                    historyViewModel.deleteBarcode(it)
                }
            }
        }
        if (aiDescriptionOpen.value && !barcode.barcode.aiGeneratedDescription.isNullOrBlank()) {
            AiDescriptionDialog(
                savedBarcode = barcode.barcode,
                viewModel = historyViewModel,
                onDismiss = { aiDescriptionOpen.value = false }
            )
        }
        if (showWifiQrCodeDialog.value) {
            WifiQrCodeDialog(
                wifiContent = barcode.barcode.barcode,
                ssid = wifiInfo?.ssid,
                onDismiss = { showWifiQrCodeDialog.value = false }
            )
        }
    }
}
