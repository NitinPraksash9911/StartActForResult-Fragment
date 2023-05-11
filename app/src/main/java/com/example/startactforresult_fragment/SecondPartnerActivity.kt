package com.example.startactforresult_fragment

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SecondPartnerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_partner)
        findViewById<TextView>(R.id.tx).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}