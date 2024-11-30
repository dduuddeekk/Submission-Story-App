package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dudek.dicodingstory.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btLogIn.setOnClickListener {
                startActivity(Intent(this@AccountActivity, LogInActivity::class.java))
            }
            btRegister.setOnClickListener {
                startActivity(Intent(this@AccountActivity, RegisterActivity::class.java))
            }
            tvGuest.setOnClickListener {
                startActivity(Intent(this@AccountActivity, MainActivity::class.java))
            }
        }
    }
}