package com.dudek.dicodingstory.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.databinding.ActivityLogInBinding
import com.dudek.dicodingstory.ui.model.AccountViewModel
import com.dudek.dicodingstory.data.service.TokenBackgroundService
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()

        accountViewModel.email.observe(this) { email ->
            binding.emailInput.setEmail(email)
        }

        accountViewModel.password.observe(this) { password ->
            binding.passwordInput.setPassword(password)
        }

        binding.goToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btLogIn.setOnClickListener {
            val email = binding.emailInput.getEmail()
            val password = binding.passwordInput.getPassword()

            binding.emailInput.setError(null)
            binding.passwordInput.setErrorMessage(null)

            accountViewModel.setEmail(email)
            accountViewModel.setPassword(password)

            if (email.isEmpty()) {
                binding.emailInput.setError("Email is empty.")
            } else if (!isValidEmail(email)) {
                binding.emailInput.setError("Invalid email format.")
            } else if (password.isEmpty()) {
                binding.passwordInput.setErrorMessage("Password is empty.")
            } else if (!isValidPassword(password)) {
                binding.passwordInput.setErrorMessage("Password must be at least 8 characters.")
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().login(email, password)
                if (response.error == false) {
                    val name = response.loginResult?.name
                    val token = response.loginResult?.token

                    if (token != null) {
                        saveTokenToSession(token)
                        startTokenService()
                    }

                    val intent = Intent(this@LogInActivity, MainActivity::class.java).apply {
                        putExtra("EXTRA_NAME", name)
                        putExtra("EXTRA_TOKEN", token)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@LogInActivity,
                        response.message ?: "Unknown error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LogInActivity,
                    "Failed to connect: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveTokenToSession(token: String) {
        val sessionPreference = SessionPreference(this)
        lifecycleScope.launch {
            sessionPreference.saveToken(token)
        }
    }

    private fun startTokenService() {
        val intent = Intent(this, TokenBackgroundService::class.java)
        startService(intent)
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivLoginCover, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val email = ObjectAnimator.ofFloat(binding.emailInput, View.ALPHA, 1f).setDuration(2000)
        val password = ObjectAnimator.ofFloat(binding.passwordInput, View.ALPHA, 1f).setDuration(2000)
        val login = ObjectAnimator.ofFloat(binding.btLogIn, View.ALPHA, 1f).setDuration(2000)

        val together = AnimatorSet().apply {
            playTogether(email, password)
        }

        AnimatorSet().apply {
            playSequentially(login, together)
            start()
        }
    }
}