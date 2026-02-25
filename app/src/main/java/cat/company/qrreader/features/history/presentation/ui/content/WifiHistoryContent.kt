package cat.company.qrreader.features.history.presentation.ui.content

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.features.history.presentation.ui.components.WifiQrCodeDialog
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.features.history.presentation.ui.components.shareBarcode
import cat.company.qrreader.utils.parseWifiContent
import java.text.SimpleDateFormat

/**
 * Content for a saved WiFi barcode in the history view.
 * Shows the SSID and security type and offers a direct connect action.
 */
@Composable
fun WifiHistoryContent(sdf: SimpleDateFormat, barcode: BarcodeModel) {
    val context = LocalContext.current
    val connectivityManager = remember { context.getSystemService(ConnectivityManager::class.java) }
    var networkCallback by remember { mutableStateOf<ConnectivityManager.NetworkCallback?>(null) }
    var showQrCodeDialog by remember { mutableStateOf(false) }

    val wifiInfo = remember(barcode.barcode) { parseWifiContent(barcode.barcode) }

    DisposableEffect(Unit) {
        onDispose {
            networkCallback?.let {
                try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            }
        }
    }

    Title(title = getTitle(barcode))
    Text(text = sdf.format(barcode.date))

    if (wifiInfo.ssid != null) {
        Text(text = stringResource(R.string.wifi_ssid, wifiInfo.ssid))
    }
    if (wifiInfo.securityType != null) {
        Text(text = stringResource(R.string.wifi_security, wifiInfo.securityType))
    }

    val isWep = wifiInfo.securityType?.uppercase() == "WEP"
    if (wifiInfo.ssid != null && !isWep) {
        TextButton(onClick = {
            networkCallback?.let {
                try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            }
            val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(wifiInfo.ssid)
            if (!wifiInfo.password.isNullOrEmpty()) {
                specifierBuilder.setWpa2Passphrase(wifiInfo.password)
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

    TextButton(onClick = { showQrCodeDialog = true }) {
        Text(text = stringResource(R.string.wifi_show_qr_code))
    }

    TextButton(onClick = { shareBarcode(context, barcode) }) {
        Text(text = stringResource(R.string.share))
    }

    if (showQrCodeDialog) {
        WifiQrCodeDialog(
            wifiContent = barcode.barcode,
            ssid = wifiInfo.ssid,
            onDismiss = { showQrCodeDialog = false }
        )
    }

    if (barcode.description != null && barcode.description.trim() != "") {
        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider()
        Text(text = barcode.description)
    }
}
