package cat.company.qrreader.domain.usecase.update

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Represents the outcome of an update check.
 */
sealed class UpdateCheckResult {
    /**
     * A newer version is available on Google Play.
     * [appUpdateInfo] can be used to start the in-app update flow.
     */
    data class UpdateAvailable(val appUpdateInfo: AppUpdateInfo) : UpdateCheckResult()

    /** The installed version is already the latest. */
    object UpToDate : UpdateCheckResult()

    /** The check could not be completed (e.g. no network, not a Play Store install). */
    data class Error(val message: String) : UpdateCheckResult()
}

/**
 * Use case that checks Google Play for a newer app version using the In-App Update API.
 *
 * Returns [UpdateCheckResult.UpdateAvailable] when Google Play reports a newer version,
 * [UpdateCheckResult.UpToDate] when already on the latest version, or
 * [UpdateCheckResult.Error] when the check cannot be completed.
 */
class CheckAppUpdateUseCase(private val appUpdateManager: AppUpdateManager) {

    suspend operator fun invoke(): UpdateCheckResult {
        return try {
            val info = appUpdateManager.appUpdateInfo.await()
            when (info.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE,
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                    UpdateCheckResult.UpdateAvailable(info)
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> UpdateCheckResult.UpToDate
                else -> UpdateCheckResult.Error("Update status unavailable")
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> if (cont.isActive) cont.resume(result) }
        addOnFailureListener { exception -> if (cont.isActive) cont.resumeWithException(exception) }
        cont.invokeOnCancellation { /* no-op – Task will still complete but the result is discarded */ }
    }
}
