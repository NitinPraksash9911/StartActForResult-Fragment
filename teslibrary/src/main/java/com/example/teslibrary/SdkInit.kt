package com.example.teslibrary

import android.content.Context
import android.widget.Toast

object SdkInit {

    @JvmStatic
    fun initSdk(context: Context) {
        Toast.makeText(context, "Helloo", Toast.LENGTH_SHORT).show()
    }
}