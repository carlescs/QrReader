package cat.company.qrreader.domain.usecase.update

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener

/**
 * Minimal [AppUpdateManager] stub whose [appUpdateInfo] task always fails,
 * used to exercise the error-handling path of [CheckAppUpdateUseCase].
 */
class FailingAppUpdateManager : AppUpdateManager {

    override fun getAppUpdateInfo(): Task<AppUpdateInfo> =
        Tasks.forException(RuntimeException("Simulated network error"))

    override fun startUpdateFlowForResult(
        appUpdateInfo: AppUpdateInfo,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        appUpdateOptions: AppUpdateOptions
    ): Boolean = false

    override fun startUpdateFlowForResult(
        appUpdateInfo: AppUpdateInfo,
        requestCode: Int,
        activity: Activity,
        appUpdateOptions: AppUpdateOptions
    ): Boolean = false

    override fun registerListener(listener: InstallStateUpdatedListener) {}

    override fun unregisterListener(listener: InstallStateUpdatedListener) {}

    override fun completeUpdate(): Task<Void> = Tasks.forResult(null)
}
