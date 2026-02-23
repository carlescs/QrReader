package cat.company.qrreader

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.ui.theme.QrReaderTheme
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.android.inject

/**
 * Main activity

 */
@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val db: BarcodesDb by inject()

    private var sharedImageUri by mutableStateOf<Uri?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        sharedImageUri = extractSharedImageUri(intent)

        enableEdgeToEdge()
        setContent {
            QrReaderTheme {
                // A surface container using the 'background' color from the theme
                MainScreen(firebaseAnalytics, sharedImageUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedImageUri = extractSharedImageUri(intent)
    }

    private fun extractSharedImageUri(intent: Intent): Uri? {
        return if (intent.action == Intent.ACTION_SEND &&
            intent.type?.startsWith("image/") == true
        ) {
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            null
        }
    }
}