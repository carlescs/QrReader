package cat.company.qrreader

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cat.company.qrreader.db.BarcodesDb
import cat.company.qrreader.ui.theme.QrReaderTheme
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Rule
import org.junit.Test

class AppTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var db: BarcodesDb
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Test
    fun testTitleIsCorrect() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        db = Room.inMemoryDatabaseBuilder(
            context, BarcodesDb::class.java).build()
        composeTestRule.setContent {
            QrReaderTheme {
                MainScreen(db = db, firebaseAnalytics = firebaseAnalytics)
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertExists()
    }

}