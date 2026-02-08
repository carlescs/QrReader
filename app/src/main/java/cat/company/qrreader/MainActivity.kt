package cat.company.qrreader

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.material3.ExperimentalMaterial3Api
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
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        enableEdgeToEdge()
        setContent {
            QrReaderTheme {
                // A surface container using the 'background' color from the theme
                MainScreen(firebaseAnalytics)
            }
        }
    }
}