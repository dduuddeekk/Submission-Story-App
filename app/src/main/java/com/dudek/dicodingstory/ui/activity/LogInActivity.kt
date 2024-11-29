package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.response.LogInResponse
import com.dudek.dicodingstory.databinding.ActivityLogInBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btLogIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                binding.apply {
                    etEmail.error = "Email is empty."
                    etPassword.error = "Password is empty."
                }
            }
        }
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

            override fun onFailure(p0: Call<LogInResponse>, t: Throwable) {
                Toast.makeText(
                    this@LogInActivity,
                    "Failed to connect: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}