package cat.company.qrreader.features.camera.presentation.ui

import android.net.Uri

/**
 * Shared-intent payload propagated from [cat.company.qrreader.MainActivity] through
 * [cat.company.qrreader.MainScreen] to [QrCameraScreen].
 *
 * Each nullable content property is paired with an *onConsumed* callback that the
 * camera screen must call once it has handled the value, so the activity can clear
 * the corresponding state and prevent re-processing on recomposition.
 *
 * @property imageUri A shared image [Uri] to scan for barcodes, or null.
 * @property onImageConsumed Called after [imageUri] has been processed.
 * @property wifiText A shared `WIFI:…` QR string, or null.
 * @property onWifiTextConsumed Called after [wifiText] has been processed.
 * @property contactText A shared vCard / MECARD string, or null.
 * @property onContactTextConsumed Called after [contactText] has been processed.
 * @property rawText Any other shared plain text (URLs, phone numbers, …), or null.
 * @property onRawTextConsumed Called after [rawText] has been processed.
 */
data class SharedContent(
    val imageUri: Uri? = null,
    val onImageConsumed: () -> Unit = {},
    val wifiText: String? = null,
    val onWifiTextConsumed: () -> Unit = {},
    val contactText: String? = null,
    val onContactTextConsumed: () -> Unit = {},
    val rawText: String? = null,
    val onRawTextConsumed: () -> Unit = {}
)
