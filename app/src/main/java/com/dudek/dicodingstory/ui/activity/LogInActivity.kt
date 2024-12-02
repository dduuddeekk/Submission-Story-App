package com.dudek.dicodingstory.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.dudek.dicodingstory.R
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.response.LogInResponse
import com.dudek.dicodingstory.databinding.ActivityLogInBinding
import com.dudek.dicodingstory.ui.model.AccountViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private var isPasswordVisible = false
    private val accountViewModel: AccountViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accountViewModel.email.observe(this, Observer { email ->
            binding.etEmail.setText(email)
        })

        accountViewModel.password.observe(this, Observer { password ->
            binding.etPassword.setText(password)
        })

        binding.etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                if (event.rawX >= binding.etPassword.right - binding.etPassword.compoundDrawables[drawableEnd].bounds.width()) {
                    togglePasswordVisibility()
                    binding.etPassword.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.btLogIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            binding.etEmail.error = null
            binding.etPassword.error = null

            accountViewModel.setEmail(email)
            accountViewModel.setPassword(password)

            if (email.isEmpty()) {
                binding.etEmail.error = "Email is empty."
            } else if (!isValidEmail(email)) {
                binding.etEmail.error = "Invalid email format."
            } else if (password.isEmpty()) {
                binding.etPassword.error = "Password is empty."
            } else if (!isValidPassword(password)) {
                binding.etPassword.error = "Password must be at least 8 characters."
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

    private fun togglePasswordVisibility() {
        val drawableStart = R.drawable.ic_lock_24
        val drawableEnd = if (isPasswordVisible) R.drawable.ic_visibility_24 else R.drawable.ic_visibility_off_24
        val inputType = if (isPasswordVisible) {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        binding.etPassword.inputType = inputType
        binding.etPassword.setCompoundDrawablesWithIntrinsicBounds(drawableStart, 0, drawableEnd, 0)
        binding.etPassword.setSelection(binding.etPassword.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    private fun loginUser(email: String, password: String) {
        val apiService = ApiConfig.getApiService()
        val call = apiService.login(email, password)

        call.enqueue(object : Callback<LogInResponse> {
            override fun onResponse(call: Call<LogInResponse>, response: Response<LogInResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.error == false) {
                        val name = loginResponse.loginResult?.name
                        val token = loginResponse.loginResult?.token

                        val intent = Intent(this@LogInActivity, MainActivity::class.java).apply {
                            putExtra("EXTRA_NAME", name)
                            putExtra("EXTRA_TOKEN", token)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LogInActivity,
                            loginResponse?.message ?: "Unknown error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LogInActivity,
                        "Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LogInResponse>, t: Throwable) {
                Toast.makeText(
                    this@LogInActivity,
                    "Failed to connect: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
