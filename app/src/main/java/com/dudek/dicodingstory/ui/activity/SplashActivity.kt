package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.databinding.ActivitySplashBinding
import com.dudek.dicodingstory.data.service.TokenBackgroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkTokenAndNavigate()
    }

    private fun checkTokenAndNavigate() {
        val sessionPreference = SessionPreference(this)

        CoroutineScope(Dispatchers.Main).launch {
            sessionPreference.token.collect { token ->
                if (token.isNullOrEmpty()) {
                    stopService(Intent(this@SplashActivity, TokenBackgroundService::class.java))
                    navigateToAccountActivity()
                } else {
                    TokenBackgroundService.startService(this@SplashActivity)
                    navigateToMainActivity(token)
                }
            }
        }
    }

    private fun navigateToAccountActivity() {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity(token: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_TOKEN, token)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }
}