package cat.company.qrreader
import android.app.Application
import cat.company.qrreader.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
class QrReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.getDefaultUncaughtExceptionHandler()?.let { defaultHandler ->
            Thread.setDefaultUncaughtExceptionHandler(
                GlobalExceptionHandler(this, defaultHandler)
            )
        }
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@QrReaderApplication)
            modules(appModules)
        }
    }
}
