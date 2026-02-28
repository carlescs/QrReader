package cat.company.qrreader.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Returns true if the device can authenticate using biometrics (fingerprint/face/iris).
 *
 * Uses [BIOMETRIC_WEAK], which covers both weak (face) and strong (fingerprint) biometrics
 * as defined in [BiometricManager.Authenticators]. Note that [DEVICE_CREDENTIAL] (PIN/pattern)
 * is intentionally excluded: combining DEVICE_CREDENTIAL with biometric authenticators prevents
 * setting a negative button text in [BiometricPrompt.PromptInfo].
 */
fun canAuthenticate(context: Context): Boolean {
    val manager = BiometricManager.from(context)
    return manager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
}

/**
 * Resolves the nearest [ComponentActivity] from a [Context], unwrapping any [ContextWrapper]
 * layers. Returns null if no [ComponentActivity] is found in the chain.
 */
fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context = this
    while (ctx !is ComponentActivity) {
        ctx = (ctx as? ContextWrapper)?.baseContext ?: return null
    }
    return ctx
}

/**
 * Shows a biometric prompt to unlock a locked barcode.
 *
 * @param activity The ComponentActivity hosting the prompt
 * @param title Prompt title
 * @param subtitle Prompt subtitle
 * @param negativeButtonText Text for the negative (cancel) button
 * @param onSuccess Called when authentication succeeds
 * @param onError Called when authentication fails or is cancelled
 */
fun showBiometricPrompt(
    activity: ComponentActivity,
    title: String,
    subtitle: String,
    negativeButtonText: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            // ERROR_USER_CANCELED and ERROR_NEGATIVE_BUTTON indicate the user chose to dismiss
            // the prompt intentionally (tapped Cancel or the negative button). These are not
            // failures worth reporting; the barcode simply remains locked.
            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
            ) {
                onError(errString.toString())
            }
        }

        override fun onAuthenticationFailed() {
            // Individual attempt failed, but user can retry - no action needed
        }
    }

    val biometricPrompt = BiometricPrompt(activity, executor, callback)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText)
        .build()

    biometricPrompt.authenticate(promptInfo)
}
