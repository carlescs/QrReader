package cat.company.qrreader.ui.components.common

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cat.company.qrreader.R
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * An icon button that attempts to connect to a WiFi network via both WPA2 and WPA3 specifiers.
 * Shows a snackbar when all connection attempts are unavailable.
 *
 * @param ssid The network SSID to connect to.
 * @param password The network password, or null / empty for open networks.
 * @param snackbarHostState Used to show an error message when the network cannot be found.
 */
@Composable
fun WifiConnectButton(
    ssid: String,
    password: String?,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val connectivityManager = remember { context.getSystemService(ConnectivityManager::class.java) }
    var networkCallback by remember { mutableStateOf<ConnectivityManager.NetworkCallback?>(null) }
    var networkCallbackWpa3 by remember { mutableStateOf<ConnectivityManager.NetworkCallback?>(null) }
    // Tracks how many pending requestNetwork calls haven't responded yet.
    // Set to Int.MAX_VALUE when any request succeeds to suppress the error snackbar.
    val pendingConnectCount = remember { AtomicInteger(0) }
    val coroutineScope = rememberCoroutineScope()
    val networkNotFound = stringResource(R.string.wifi_network_not_found)

    DisposableEffect(Unit) {
        onDispose {
            networkCallback?.let {
                try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            }
            networkCallbackWpa3?.let {
                try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
            }
        }
    }

    IconButton(onClick = {
        networkCallback?.let {
            try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
        }
        networkCallback = null
        networkCallbackWpa3?.let {
            try { connectivityManager.unregisterNetworkCallback(it) } catch (_: Exception) {}
        }
        networkCallbackWpa3 = null

        val hasPassword = !password.isNullOrEmpty()
        pendingConnectCount.set(if (hasPassword) 2 else 1)

        // WPA2 / transition-mode request
        try {
            val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(ssid)
            if (hasPassword) specifierBuilder.setWpa2Passphrase(password!!)
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifierBuilder.build())
                .build()
            val wpa2Callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // A successful connection — disable error snackbar for remaining callbacks
                    pendingConnectCount.set(Int.MAX_VALUE)
                }
                override fun onUnavailable() {
                    // == 0 ensures only one callback fires the snackbar even if both fail simultaneously
                    if (pendingConnectCount.decrementAndGet() == 0) {
                        coroutineScope.launch { snackbarHostState.showSnackbar(networkNotFound) }
                    }
                }
            }
            connectivityManager.requestNetwork(request, wpa2Callback)
            // Store after successful registration to avoid unregistering an unregistered callback
            networkCallback = wpa2Callback
        } catch (_: Exception) {
            if (pendingConnectCount.decrementAndGet() == 0) {
                coroutineScope.launch { snackbarHostState.showSnackbar(networkNotFound) }
            }
        }

        // WPA3-only request (concurrent, for WPA3-only APs)
        if (hasPassword) {
            try {
                val wpa3Specifier = WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa3Passphrase(password!!)
                    .build()
                val wpa3Request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(wpa3Specifier)
                    .build()
                val wpa3Callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        pendingConnectCount.set(Int.MAX_VALUE)
                    }
                    override fun onUnavailable() {
                        if (pendingConnectCount.decrementAndGet() == 0) {
                            coroutineScope.launch { snackbarHostState.showSnackbar(networkNotFound) }
                        }
                    }
                }
                connectivityManager.requestNetwork(wpa3Request, wpa3Callback)
                networkCallbackWpa3 = wpa3Callback
            } catch (_: Exception) {
                if (pendingConnectCount.decrementAndGet() == 0) {
                    coroutineScope.launch { snackbarHostState.showSnackbar(networkNotFound) }
                }
            }
        }
    }) {
        Icon(
            imageVector = Icons.Filled.Wifi,
            contentDescription = stringResource(R.string.wifi_connect)
        )
    }
}
