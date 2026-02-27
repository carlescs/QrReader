package cat.company.qrreader

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.ui.theme.QrReaderTheme
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Main activity
 */
@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val db: BarcodesDb by inject()

    private val _sharedImageUri = MutableStateFlow<Uri?>(null)
    private val _sharedText = MutableStateFlow<String?>(null)
    private val _sharedContactText = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        _sharedImageUri.value = extractSharedImageUri(intent)
        _sharedText.value = extractSharedText(intent)
        lifecycleScope.launch { _sharedContactText.value = extractSharedContactText(intent) }

        enableEdgeToEdge()
        setContent {
            QrReaderTheme {
                val sharedImageUri by _sharedImageUri.collectAsState()
                val sharedText by _sharedText.collectAsState()
                val sharedContactText by _sharedContactText.collectAsState()
                MainScreen(
                    firebaseAnalytics,
                    sharedImageUri,
                    onSharedImageConsumed = { _sharedImageUri.value = null },
                    sharedText = sharedText,
                    onSharedTextConsumed = { _sharedText.value = null },
                    sharedContactText = sharedContactText,
                    onSharedContactTextConsumed = { _sharedContactText.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _sharedImageUri.value = extractSharedImageUri(intent)
        _sharedText.value = extractSharedText(intent)
        lifecycleScope.launch { _sharedContactText.value = extractSharedContactText(intent) }
    }

    private fun extractSharedImageUri(intent: Intent): Uri? {
        if (intent.action != Intent.ACTION_SEND) return null
        if (intent.type?.startsWith("image/") != true) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }

    private fun extractSharedText(intent: Intent): String? {
        if (intent.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (text.isNullOrEmpty()) return null
        return if (text.startsWith("WIFI:", ignoreCase = true)) text else null
    }

    /**
     * Extracts a vCard contact string from a shared intent.
     *
     * Handles two sharing scenarios:
     * - `text/x-vcard` with [Intent.EXTRA_STREAM]: reads the file content from the URI.
     * - `text/plain` with [Intent.EXTRA_TEXT]: returns the text if it starts with
     *   `BEGIN:VCARD` or `MECARD:`.
     */
    private suspend fun extractSharedContactText(intent: Intent): String? {
        if (intent.action != Intent.ACTION_SEND) return null

        if (intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
            if (!text.isNullOrEmpty() && (
                    text.startsWith("BEGIN:VCARD", ignoreCase = true) ||
                    text.startsWith("MECARD:", ignoreCase = true)
                )
            ) return text
            return null
        }

        if (intent.type == "text/x-vcard" || intent.type == "text/vcard") {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            uri ?: return null
            return withContext(Dispatchers.IO) {
                try {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.readText()?.trim()
                } catch (e: Exception) {
                    android.util.Log.w("MainActivity", "Failed to read shared vCard URI", e)
                    null
                }
            }
        }

        return null
    }
}