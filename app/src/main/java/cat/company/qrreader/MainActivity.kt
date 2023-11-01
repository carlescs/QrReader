package cat.company.qrreader

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.material3.*
import androidx.room.Room
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.db.Migrations
import cat.company.qrreader.ui.theme.QrReaderTheme

@ExperimentalGetImage
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room
            .databaseBuilder(applicationContext, BarcodesDb::class.java, "barcodes_db")
            .addMigrations(
                Migrations.MIGRATION_1_2,
                Migrations.MIGRATION_2_3,
                Migrations.MIGRATION_3_4
            )
            .build()
        setContent {
            QrReaderTheme {
                // A surface container using the 'background' color from the theme
                MainScreen(db)
            }
        }
    }
}