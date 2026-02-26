package cat.company.qrreader.features.history.presentation.ui.content

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.company.qrreader.R
import cat.company.qrreader.domain.model.BarcodeModel
import cat.company.qrreader.features.camera.presentation.ui.components.Title
import cat.company.qrreader.features.history.presentation.ui.components.getTitle
import cat.company.qrreader.utils.parseWifiContent
import java.text.SimpleDateFormat

/**
 * Content for a saved WiFi barcode in the history view.
 * Shows the SSID and security type. Connect and QR code actions are in the card toolbar.
 */
@Composable
fun WifiHistoryContent(sdf: SimpleDateFormat, barcode: BarcodeModel) {
    val wifiInfo = remember(barcode.barcode) { parseWifiContent(barcode.barcode) }

    Title(title = getTitle(barcode))
    Text(text = sdf.format(barcode.date))

    if (wifiInfo.ssid != null) {
        Text(text = stringResource(R.string.wifi_ssid, wifiInfo.ssid))
    }
    if (wifiInfo.securityType != null) {
        Text(text = stringResource(R.string.wifi_security, wifiInfo.securityType))
    }

    if (barcode.description != null && barcode.description.trim() != "") {
        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider()
        Text(text = barcode.description)
    }
}
