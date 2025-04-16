package com.example.pocketbrain.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketbrain.R

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 1500L // 1.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar if it exists
        supportActionBar?.hide()

        // Use Handler to delay the launch of MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the main activity
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)

            // Close the splash activity
            finish()

            // Add a smooth transition animation
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, SPLASH_DELAY)
    }
}