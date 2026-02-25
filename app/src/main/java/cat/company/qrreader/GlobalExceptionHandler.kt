package cat.company.qrreader

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Global uncaught exception handler that prevents a hard crash by restarting the app gracefully
 * while still ensuring the crash is reported to Firebase Crashlytics (visible in Google Play
 * Console under Android Vitals / Crashes & ANRs).
 *
 * ## Reporting flow
 * Firebase Crashlytics installs its own [Thread.UncaughtExceptionHandler] during app startup
 * (via `FirebaseInitProvider`, a `ContentProvider` that runs before [android.app.Application.onCreate]).
 * When [QrReaderApplication] registers this handler it captures that Crashlytics handler as
 * [defaultHandler], so the full chain on an uncaught exception is:
 *
 * ```
 * GlobalExceptionHandler
 *   → FirebaseCrashlytics.recordException()   // enqueues the crash report
 *   → defaultHandler (Crashlytics fatal handler) // marks as fatal & delegates to Android
 *     → Android system handler                   // writes tombstone, records Android Vitals
 *       → Process.killProcess()                  // terminates the process
 * ```
 *
 * The [PendingIntent] scheduled in step 1 is then fired by the Android system, restarting the
 * app instead of showing the "app has stopped" dialog.
 *
 * Registered in [QrReaderApplication.onCreate] as the default handler for all threads.
 */
class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "Uncaught exception on thread '${thread.name}'", throwable)

            // Enqueue the crash with Crashlytics so it reaches Firebase / Google Play Console.
            FirebaseCrashlytics.getInstance().recordException(throwable)

            val restartIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent.send()
        } catch (secondary: Exception) {
            Log.e(TAG, "Failed to schedule restart after crash", secondary)
        } finally {
            // Delegate to the previous default handler (Crashlytics' fatal handler → Android
            // system handler) so platform-level crash recording still takes place.
            defaultHandler.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
    }
}
