package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        accountViewModel.name.observe(this) { name ->
            binding.nameInput.setName(name)
        }
        accountViewModel.email.observe(this) { email ->
            binding.emailInput.setEmail(email)
        }
        accountViewModel.password.observe(this) { password ->
            binding.passwordInput.setPassword(password)
        }

        binding.btRegister.setOnClickListener {
            val name = binding.nameInput.getName()
            val email = binding.emailInput.getEmail()
            val password = binding.passwordInput.getPassword()

            binding.nameInput.setError(null)
            binding.emailInput.setError(null)
            binding.passwordInput.setError(null)

            accountViewModel.setName(name)
            accountViewModel.setEmail(email)
            accountViewModel.setPassword(password)

            if (name.isEmpty()) {
                binding.nameInput.setError("Name is empty.")
            }
            if (email.isEmpty()) {
                binding.emailInput.setError("Email is empty.")
            }
            if (password.isEmpty()) {
                binding.passwordInput.setError("Password is empty.")
            }

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidEmail(email) && isValidPassword(password)) {
                    registerUser(name, email, password)
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return if (!emailPattern.matcher(email).matches()) {
            binding.emailInput.setError("Invalid email format")
            false
        } else {
            true
        }
    }

    private fun isValidPassword(password: String): Boolean {
        return if (password.length < 8) {
            binding.passwordInput.setError("Password must be at least 8 characters")
            false
        } else {
            true
        }
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