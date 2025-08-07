package cat.company.qrreader

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.room.Room
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.Migrations
import cat.company.qrreader.ui.theme.QrReaderTheme
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Main activity

 */
@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val db = Room
            .databaseBuilder(applicationContext, BarcodesDb::class.java, "barcodes_db")
            .addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3,
                Migrations.MIGRATION_3_4
            )
            .build()
        enableEdgeToEdge()
        setContent {
            QrReaderTheme {
                // A surface container using the 'background' color from the theme
                MainScreen(db, firebaseAnalytics)
            }
        }
    }
}