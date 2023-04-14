package com.example.startactforresult_fragment.util

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthUtil(private val activity: Activity) {

    companion object {
        const val TAG = "authCallback"
    }

    private val launchPinOrPatternPrompt =
        (activity as FragmentActivity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // User has successfully authenticated with PIN or pattern
                onSuccess.invoke()
            } else {
                onError.invoke("Wrong Credential")
            }
        }

    private val biometricPrompt: BiometricPrompt
    private var onSuccess: () -> Unit = {}
    private var onError: (String) -> Unit = {}

    init {
        biometricPrompt = generateBiometricPrompt()
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showLog(TAG, "App can authenticate using biometrics.")
                true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showLog(TAG, "No Biometric sensor found on this device.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showLog(TAG, "No Biometric senor is currently unavailable.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showLog(TAG, "No Biometric credential enrolled on this device.")
                false
            }
            else -> false
        }
    }

    private fun isDeviceLockAvailable(): Boolean {
        val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    fun authenticViaDeviceBiometricOrPin(onSuccess: () -> Unit, onError: (String) -> Unit) {
        this.onError = onError
        this.onSuccess = onSuccess

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && isBiometricAvailable()) {
            // Authenticate via biometric if available
            biometricPrompt.authenticate(generatePromptInfo())
        } else if (isDeviceLockAvailable()) {
            // Prompt for PIN or pattern if device lock is available
            showPinOrPatternPrompt()
        } else {
            // Invoke onSuccess if neither biometric nor device lock is available
            onSuccess.invoke()
        }
    }

    private fun showPinOrPatternPrompt() {
        val keyguardManager =
            activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
            "Authentication required",
            "Please enter your PIN or pattern to continue"
        )
        launchPinOrPatternPrompt.launch(intent)
    }



    private fun generateBiometricPrompt(): BiometricPrompt {

        return BiometricPrompt(activity as FragmentActivity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    showDebugToast(activity, "Authentication error: $errString")
                    showLog(TAG, "Authentication error: $errString ")
                    onError.invoke(errString.toString())
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    showDebugToast(activity, "Authentication succeeded!")
                    showLog(TAG, "Authentication was successful")
                    onSuccess.invoke()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showDebugToast(activity, "Authentication failed")
                    showLog(TAG, "Authentication failed")
                    onError.invoke("Authentication failed")
                }
            })

    }

    private fun generatePromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder().setTitle("Biometric login")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

}