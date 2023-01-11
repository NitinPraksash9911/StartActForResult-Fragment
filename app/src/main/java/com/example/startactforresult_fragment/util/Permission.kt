package com.example.startactforresult_fragment.util

import android.Manifest.permission.*

sealed class Permission(vararg val permissions: String) {
    // Individual permissions
    object SendSMS : Permission(SEND_SMS)
    object ReadPhoneState : Permission(READ_PHONE_STATE)


    companion object {
        fun from(permission: String) = when (permission) {
            SEND_SMS -> SendSMS
            READ_PHONE_STATE -> ReadPhoneState
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}