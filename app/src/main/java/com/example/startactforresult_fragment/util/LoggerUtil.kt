package com.example.startactforresult_fragment.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.startactforresult_fragment.BuildConfig

internal fun showLog(tag: String = "upswing_sdk", message: String?) {
    message?.let {
        if (isDebuggable())
            printFullLog(message, tag)
    }
}

internal fun showDebugToast(context: Context, message: String) {
    if (isDebuggable()) {
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
    }

}

internal fun showLiveToast(context: Context, message: String) {
    Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
}

internal fun Exception.showLog() {
    if (isDebuggable()) {
        printStackTrace()
    }
}

internal fun Throwable.showLog() {
    if (isDebuggable()) {
        printStackTrace()
    }
}

private fun printFullLog(message: String, tag: String) {
    if (message.length > 3000) {
        Log.v(tag, message.substring(0, 3000))
        printFullLog(message.substring(3000), tag)
    } else {
        Log.v(tag, message)
    }
}

private fun isDebuggable() = BuildConfig.DEBUG