package com.dudek.dicodingstory.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dudek.dicodingstory.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()

        binding.apply {
            btLogIn.setOnClickListener {
                startActivity(Intent(this@AccountActivity, LogInActivity::class.java))
            }
            btRegister.setOnClickListener {
                startActivity(Intent(this@AccountActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivCoverImage, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val login = ObjectAnimator.ofFloat(binding.btLogIn, View.ALPHA, 4f).setDuration(4000)
        val register = ObjectAnimator.ofFloat(binding.btRegister, View.ALPHA, 4f).setDuration(4000)
        val title = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 2f).setDuration(2000)
        val desc = ObjectAnimator.ofFloat(binding.tvDescriptionWelcome, View.ALPHA, 2f).setDuration(2000)

        val together = AnimatorSet().apply {
            playTogether(login, register)
        }

        AnimatorSet().apply {
            playSequentially(title, desc, together)
            start()
        }
    }
}