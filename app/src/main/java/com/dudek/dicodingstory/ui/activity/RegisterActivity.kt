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
import com.dudek.dicodingstory.data.response.RegisterResponse
import com.dudek.dicodingstory.databinding.ActivityRegisterBinding
import com.dudek.dicodingstory.ui.model.AccountViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false
    private val accountViewModel: AccountViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accountViewModel.name.observe(this) { name ->
            binding.etName.setText(name)
        }

        accountViewModel.email.observe(this) { email ->
            binding.etEmail.setText(email)
        }

        accountViewModel.password.observe(this) { password ->
            binding.etPassword.setText(password)
        }

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

        binding.btRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            accountViewModel.setName(name)
            accountViewModel.setEmail(email)
            accountViewModel.setPassword(password)

            binding.apply {
                etName.error = null
                etEmail.error = null
                etPassword.error = null
            }

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidEmail(email) && isValidPassword(password)) {
                    registerUser(name, email, password)
                }
            } else {
                binding.apply {
                    if (name.isEmpty()) etName.error = "Name is empty."
                    if (email.isEmpty()) etEmail.error = "Email is empty."
                    if (password.isEmpty()) etPassword.error = "Password is empty."
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return if (!emailPattern.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            false
        } else {
            true
        }
    }

    private fun isValidPassword(password: String): Boolean {
        return if (password.length < 8) {
            binding.etPassword.error = "Password must be at least 8 characters"
            false
        } else {
            true
        }
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

    private fun registerUser(name: String, email: String, password: String) {
        val apiService = ApiConfig.getApiService()
        val call = apiService.register(name, email, password)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null && registerResponse.error == false) {
                        Toast.makeText(
                            this@RegisterActivity,
                            registerResponse.message ?: "Registration successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@RegisterActivity, LogInActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            registerResponse?.message ?: "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Failed to connect: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
