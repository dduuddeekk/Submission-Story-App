package com.dudek.dicodingstory.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.data.service.TokenBackgroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkTokenAndNavigate()
    }

    private fun checkTokenAndNavigate() {
        val sessionPreference = SessionPreference(this)

        CoroutineScope(Dispatchers.Main).launch {
            sessionPreference.token.collect { token ->
                Handler(Looper.getMainLooper()).postDelayed({
                    if (token.isNullOrEmpty()) {
                        stopService(Intent(this@SplashActivity, TokenBackgroundService::class.java))
                        navigateToLogInActivity()
                    } else {
                        TokenBackgroundService.startService(this@SplashActivity)
                        navigateToMainActivity(token)
                    }
                }, 2000)
            }
        }
    }

    private fun navigateToLogInActivity() {
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity(token: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_TOKEN, token)
        }
        startActivity(intent)
        finish()
    }
}