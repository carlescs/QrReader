package cat.company.qrreader.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Returns true if the device can authenticate using biometrics or device credentials.
 */
fun canAuthenticate(context: Context): Boolean {
    val manager = BiometricManager.from(context)
    val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    return manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
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
