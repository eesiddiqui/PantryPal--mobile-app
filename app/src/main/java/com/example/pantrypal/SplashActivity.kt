package com.example.pantrypal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * SplashActivity – shown at app launch for 2 seconds, then navigates to HomeActivity.
 * Uses Handler.postDelayed for the timed transition.
 */
class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make status bar transparent and show full-screen
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        setContentView(R.layout.activity_splash)

        // Navigate to HomeActivity after 2.2 seconds
        handler.postDelayed({
            if (!isFinishing) {
                startActivity(Intent(this, HomeActivity::class.java))
                // Smooth fade-in transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }, 2200)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
