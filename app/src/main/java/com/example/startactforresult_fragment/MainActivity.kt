package com.example.startactforresult_fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.startactforresult_fragment.util.BiometricAuthUtil
import com.example.startactforresult_fragment.util.showDebugToast
import com.example.teslibrary.SdkInit
import com.google.gson.Gson
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences
    lateinit var file: File
    lateinit var gson: Gson
    lateinit var encryptedFile: EncryptedFile
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gson = Gson()

        file = File(this.filesDir, "test.json")
        createSharedPref()


        findViewById<Button>(R.id.btn_save).setOnClickListener {
//            saveICI("ICIQ9Q66Q8W868612")
            SdkInit.initSdk(context = this)
        }

        findViewById<Button>(R.id.btn_save2).setOnClickListener {
            saveGST("GST98912heiwh")
        }

        findViewById<Button>(R.id.btn_read).setOnClickListener {
            val ICI = sharedPref.getString("ICI", "")
            val GST = sharedPref.getString("GST", "")
            Log.d("Asdasdasdadas", "onCreate:ICI $ICI:,   GST: $GST")
        }


        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            with(sharedPref.edit()) {
                clear()
                apply()
            }
            val ICI = sharedPref.getString("ICI", "")
            val GST = sharedPref.getString("GST", "")
            Log.d("Asdasdasdadas", "onCreate:ICI $ICI:,   GST: $GST")
        }

        checkBiometric()
    }

    private fun checkBiometric() {
        val biometricAuthUtil = BiometricAuthUtil(this)
        biometricAuthUtil.authenticViaDeviceBiometricOrPin(onSuccess = {
            showDebugToast(this, "Auth success")
        }, onError = {
            showDebugToast(this, "Auth fialed")
        })
    }

    private fun saveICI(value: String) {
        with(sharedPref.edit()) {
            putString("ICI", value)
            apply()
        }
        if (sharedPref.getString("", null).equals(null)) {

        }
    }

    private fun saveGST(value: String) {
        with(sharedPref.edit()) {
            putString("GST", value)
            apply()
        }
    }

    private fun createSharedPref() {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        sharedPref = EncryptedSharedPreferences.create(
            "upswing_pref",
            mainKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


}