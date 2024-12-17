package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.databinding.ActivityMainBinding
import com.dudek.dicodingstory.ui.adapter.StoriesAdapter
import com.dudek.dicodingstory.data.service.TokenBackgroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra(EXTRA_TOKEN)

        binding.apply {
            fabAddStory.setOnClickListener {
                val intent = Intent(this@MainActivity, NewStoryActivity::class.java)
                intent.putExtra(EXTRA_TOKEN, token)
                startActivity(intent)
            }

            rvStoryContainer.layoutManager = LinearLayoutManager(this@MainActivity)
            fetchStories()

            icMapView.setOnClickListener {
                val intent = Intent(this@MainActivity, MapsView::class.java)
                intent.putExtra(EXTRA_TOKEN, token)
                startActivity(intent)
            }

            icLogout.setOnClickListener {
                stopService(Intent(this@MainActivity, TokenBackgroundService::class.java))

                val sessionPreference = SessionPreference(this@MainActivity)
                lifecycleScope.launch(Dispatchers.IO) {
                    sessionPreference.saveToken("")
                }

                token = null

                val intent = Intent(this@MainActivity, SplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun fetchStories() {
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().getAllStories("Bearer $token")
                if (!response.error!!) {
                    val stories = response.listStory?.filterNotNull() ?: emptyList()

                    binding.rvStoryContainer.adapter = StoriesAdapter(stories, token)
                    binding.progressBar.visibility = View.GONE
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load stories", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    companion object {
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }
}