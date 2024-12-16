package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.data.response.StoriesResponse
import com.dudek.dicodingstory.data.service.TokenBackgroundService
import com.dudek.dicodingstory.databinding.ActivityMainBinding
import com.dudek.dicodingstory.ui.adapter.StoriesAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                CoroutineScope(Dispatchers.IO).launch {
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

        ApiConfig.getApiService().getAllStories("Bearer $token").enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(call: Call<StoriesResponse>, response: Response<StoriesResponse>) {
                if (response.isSuccessful) {
                    val stories = response.body()?.listStory ?: emptyList()
                    binding.rvStoryContainer.adapter = StoriesAdapter(stories, token)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load stories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }
}
