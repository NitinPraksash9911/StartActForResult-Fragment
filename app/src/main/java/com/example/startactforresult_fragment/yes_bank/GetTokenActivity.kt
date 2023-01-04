package com.example.startactforresult_fragment.yes_bank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.startactforresult_fragment.R

class GetTokenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_token)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = Intent()
        if (intent != null) {
            if (intent.hasExtra("key")) {
                val key = intent.getIntExtra("key", 0)
                if (key % 2 == 0) {
                    data.putExtra("success", 201)
                    setResult(RESULT_OK, data)
                } else {
                    data.putExtra("error", 404)
                    setResult(RESULT_CANCELED, data)
                }
            }
        }
        finish()
    }
}