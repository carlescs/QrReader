package cat.company.qrreader.features.lock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.company.qrreader.domain.usecase.settings.GetAppLockEnabledUseCase
import cat.company.qrreader.domain.usecase.settings.GetAutoLockOnFocusLossUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for the app-level lock screen.
 *
 * Survives orientation changes so biometric unlock state is preserved across re-creation.
 * Lock state is modelled as a nullable Boolean:
 * - `null`  – initial check in progress (show lock screen without unlock button)
 * - `true`  – app is locked (show lock screen with unlock button)
 * - `false` – app is unlocked (show normal content)
 */
class AppLockViewModel(
    private val getAppLockEnabled: GetAppLockEnabledUseCase,
    private val getAutoLockOnFocusLoss: GetAutoLockOnFocusLossUseCase
) : ViewModel() {

    private val _isLocked = MutableStateFlow<Boolean?>(null)

    /**
     * Current lock state:
     * - `null`  – determining (show lock screen as a safe default)
     * - `true`  – locked
     * - `false` – unlocked
     */
    val isLocked: StateFlow<Boolean?> = _isLocked.asStateFlow()

    /**
     * Called from [MainActivity.onCreate] to determine the initial lock state.
     *
     * @param isRestoredInstance `true` when the Activity is being recreated (e.g. rotation).
     *   In that case, if the user had already unlocked this session, we keep them unlocked.
     */
    fun checkInitialLockState(isRestoredInstance: Boolean) {
        viewModelScope.launch {
            val enabled = getAppLockEnabled().first()
            if (!enabled) {
                _isLocked.value = false
                return@launch
            }
            // If this is a config-change recreation and the user already unlocked, keep unlocked.
            if (isRestoredInstance && _isLocked.value == false) {
                return@launch
            }
            _isLocked.value = true
        }
    }

    /**
     * Called from [MainActivity.onStop] when the app is going to the background.
     * Locks the app if both app-lock and auto-lock-on-focus-loss settings are enabled.
     */
    fun lockIfAutoLockEnabled() {
        viewModelScope.launch {
            val appLock = getAppLockEnabled().first()
            val autoLock = getAutoLockOnFocusLoss().first()
            if (appLock && autoLock) {
                _isLocked.value = true
            }
        }
    }

    /**
     * Marks the app as unlocked after successful biometric authentication.
     */
    fun unlock() {
        _isLocked.value = false
    }
}
