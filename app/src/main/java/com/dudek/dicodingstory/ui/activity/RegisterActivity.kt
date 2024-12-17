package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.databinding.ActivityRegisterBinding
import com.dudek.dicodingstory.ui.model.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        binding.btRegister.setOnClickListener {
            validateAndRegister()
        }
    }

    private fun observeViewModel() {
        accountViewModel.name.observe(this) { name ->
            binding.nameInput.setName(name)
        }
        accountViewModel.email.observe(this) { email ->
            binding.emailInput.setEmail(email)
        }
        accountViewModel.password.observe(this) { password ->
            binding.passwordInput.setPassword(password)
        }
    }

    private fun validateAndRegister() {
        val name = binding.nameInput.getName()
        val email = binding.emailInput.getEmail()
        val password = binding.passwordInput.getPassword()

        binding.nameInput.setError(null)
        binding.emailInput.setError(null)
        binding.passwordInput.setErrorMessage(null)

        accountViewModel.setName(name)
        accountViewModel.setEmail(email)
        accountViewModel.setPassword(password)

        when {
            name.isEmpty() -> binding.nameInput.setError("Name is empty.")
            email.isEmpty() -> binding.emailInput.setError("Email is empty.")
            !isValidEmail(email) -> binding.emailInput.setError("Invalid email format.")
            password.isEmpty() -> binding.passwordInput.setErrorMessage("Password is empty.")
            !isValidPassword(password) -> binding.passwordInput.setErrorMessage("Password must be at least 8 characters.")
            else -> registerUser(name, email, password)
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

    private fun registerUser(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().register(name, email, password)
                }
                if (response.error == false) {
                    Toast.makeText(
                        this@RegisterActivity,
                        response.message ?: "Registration successful!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@RegisterActivity, LogInActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        response.message ?: "Registration failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Failed to connect: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}