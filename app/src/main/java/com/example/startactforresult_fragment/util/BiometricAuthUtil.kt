package com.example.startactforresult_fragment.util

import android.app.Activity
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthUtil(private val activity: Activity) {

    companion object {
        const val TAG = "authCallback"
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")
                true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e(TAG, "No Biometric sensor found on this device.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e(TAG, "No Biometric senor is currently unavailable.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e(TAG, "No Biometric credential enrolled on this device.")
                false
            }
            else -> false
        }
    }

     fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "$errorCode :: $errString")
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
//                    loginWithPassword() // Because in this app, the negative button allows the user to enter an account password. This is completely optional and your app doesn’t have to do it.
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                // Proceed with viewing the private encrypted message.
//                showEncryptedMessage(result.cryptoObject)
            }
        }

        return BiometricPrompt(activity as FragmentActivity, executor, callback)
    }

    fun generatePromptInfo() =
        try {
            BiometricPrompt.PromptInfo.Builder().setTitle("Biometric login for Upswing")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()
        } catch (e: Exception) {
            BiometricPrompt.PromptInfo.Builder().setTitle("Biometric login for Upswing")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("cancel")
                .setAllowedAuthenticators(DEVICE_CREDENTIAL)
                .build()
        }

}