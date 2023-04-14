package com.example.startactforresult_fragment.util

import android.Manifest
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.SEND_SMS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class PermissionManager private constructor(private val fragment: WeakReference<Fragment>) {

    private val requiredPermissions = mutableListOf<Permission>()
    private var resultCallback: (PermissionStatus) -> Unit = {}

    private val permissionCheck = fragment.get()
        ?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            sendResultAndCleanUp(grantResults)
        }

    companion object {
        fun from(fragment: Fragment) = PermissionManager(WeakReference(fragment))
    }

    fun request(vararg permission: Permission): PermissionManager {
        requiredPermissions.addAll(permission)
        handlePermissionRequest()
        return this
    }

    fun resultCallback(callback: (PermissionStatus) -> Unit = {}): PermissionManager {
        this.resultCallback = callback
        return this
    }

    private fun handlePermissionRequest() {
        fragment.get()?.let { fragment ->
            when {
                areAllPermissionsGranted(fragment) -> sendPositiveResult()
                else -> requestPermissions()
            }
        }
    }

    private fun handlePermissionDenied() {
        fragment.get()?.let { fragment ->
            if (shouldShowPermissionRationale(fragment).not()) {
                resultCallback.invoke(PermissionStatus.NEVER_ASK)
            } else {
                resultCallback.invoke(PermissionStatus.DENIED)
            }
        }
    }

    fun allowPermissionsFromSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", fragment.get()?.context?.packageName ?: "", null)
        }
        fragment.get()?.startActivity(intent)
    }


    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associate { it to true })
    }

    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        if (grantResults.values.any { !it }) {
            handlePermissionDenied()
        } else {
            resultCallback.invoke(PermissionStatus.GRANTED)
        }
        cleanUp()
    }

    private fun cleanUp() {
        requiredPermissions.clear()
        resultCallback = {}
    }

    private fun requestPermissions() {
        permissionCheck?.launch(getPermissionList())
    }

    private fun areAllPermissionsGranted(fragment: Fragment) =
        requiredPermissions.all { it.isGranted(fragment) }

    private fun shouldShowPermissionRationale(fragment: Fragment) =
        requiredPermissions.any { it.requiresRationale(fragment) }

    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

    private fun Permission.isGranted(fragment: Fragment) =
        permissions.all { hasPermission(fragment, it) }

    private fun Permission.requiresRationale(fragment: Fragment) =
        permissions.any { fragment.shouldShowRequestPermissionRationale(it) }

    private fun hasPermission(fragment: Fragment, permission: String) =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED

    fun hasReadPhoneStatePermission(): Boolean {
        return fragment.get()?.let {
            hasPermission(it, READ_PHONE_STATE)
        } ?: false
    }

    fun hasSmsPermission(): Boolean {
        return fragment.get()?.let {
            hasPermission(it, SEND_SMS)
        } ?: false
    }

    fun hasReadAttachmentsPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            fragment.get()?.let {
                hasPermission(it, Manifest.permission.READ_MEDIA_IMAGES)
            } ?: false
        } else {
            fragment.get()?.let {
                hasPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
            } ?: false
        }
    }

}
