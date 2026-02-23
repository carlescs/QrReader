package cat.company.qrreader.features.history.presentation.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cat.company.qrreader.R
import cat.company.qrreader.domain.usecase.codecreator.GenerateQrCodeUseCase
import cat.company.qrreader.domain.usecase.codecreator.SaveBitmapToMediaStoreUseCase
import org.koin.compose.koinInject

/**
 * Dialog that displays a WiFi network as a scannable QR code and allows sharing it.
 *
 * @param wifiContent The raw WiFi QR string (e.g. `WIFI:T:WPA;S:MyNet;P:pass;;`).
 * @param ssid The network SSID shown as the dialog title.
 * @param onDismiss Called when the dialog is dismissed.
 */
@Composable
fun WifiQrCodeDialog(
    wifiContent: String,
    ssid: String?,
    onDismiss: () -> Unit,
    generateQrCodeUseCase: GenerateQrCodeUseCase = koinInject(),
    saveBitmapToMediaStoreUseCase: SaveBitmapToMediaStoreUseCase = koinInject()
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(wifiContent) {
        bitmap = generateQrCodeUseCase(wifiContent)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (ssid != null) {
                    Text(
                        text = stringResource(R.string.wifi_ssid, ssid),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                val currentBitmap = bitmap
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        val uri = saveBitmapToMediaStoreUseCase(context, currentBitmap)
                        if (uri == null) {
                            Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.wifi_share_qr_code)))
                    }) {
                        Text(text = stringResource(R.string.wifi_share_qr_code))
                    }
                } else {
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    }
}
