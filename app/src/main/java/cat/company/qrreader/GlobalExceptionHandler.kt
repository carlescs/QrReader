package cat.company.qrreader

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Global uncaught exception handler that prevents a hard crash by restarting the app gracefully.
 *
 * Registered in [QrReaderApplication.onCreate] as the default handler for all threads.
 * When an unhandled exception escapes to the top of any thread, this handler:
 * 1. Logs the throwable so it is visible in Logcat.
 * 2. Schedules a clean relaunch of [MainActivity] via a [PendingIntent].
 * 3. Terminates the current process — the scheduled intent is then fired by the system,
 *    restarting the app instead of showing the "app has stopped" system dialog.
 *
 * The [defaultHandler] is invoked **before** restarting so that any platform-level
 * crash handling (e.g. writing a tombstone) still takes place.
 */
class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "Uncaught exception on thread '${thread.name}'", throwable)

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
            defaultHandler.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
    }
}
