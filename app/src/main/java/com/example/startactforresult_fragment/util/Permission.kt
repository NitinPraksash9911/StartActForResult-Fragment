package com.example.startactforresult_fragment.util

import android.Manifest.permission.*

sealed class Permission(vararg val permissions: String) {
    object SendSMS : Permission(SEND_SMS)
    object ReadPhoneState : Permission(READ_PHONE_STATE)
    object ReadExternalStorage : Permission(READ_EXTERNAL_STORAGE)
    object ReadMediaImages : Permission(READ_MEDIA_IMAGES)

}