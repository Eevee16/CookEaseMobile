package com.cookease.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect immediately — LoginFragment in nav graph handles auth
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}