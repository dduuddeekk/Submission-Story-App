package com.dudek.dicodingstory.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.databinding.ActivityMainBinding
import com.dudek.dicodingstory.ui.adapter.StoriesAdapter
import com.dudek.dicodingstory.data.service.TokenBackgroundService
import com.dudek.dicodingstory.ui.adapter.LoadingStateAdapter
import com.dudek.dicodingstory.ui.factory.MainViewModelFactory
import com.dudek.dicodingstory.ui.model.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var token: String? = null

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory.getInstance(this)
    }

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

            icMapView.setOnClickListener {
                val intent = Intent(this@MainActivity, MapsView::class.java)
                intent.putExtra(EXTRA_TOKEN, token)
                startActivity(intent)
            }

            icLogout.setOnClickListener {
                stopService(Intent(this@MainActivity, TokenBackgroundService::class.java))

                lifecycleScope.launch(Dispatchers.IO) {
                    SessionPreference(this@MainActivity).saveToken("")
                }

                token = null

                val intent = Intent(this@MainActivity, SplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val storiesAdapter = StoriesAdapter(token)

        binding.rvStoryContainer.adapter = storiesAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter { storiesAdapter.retry() }
        )

        if (!token.isNullOrEmpty()) {
            mainViewModel.stories.observe(this) { pagingData ->
                storiesAdapter.submitData(lifecycle, pagingData)
            }
        } else {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
        }

        storiesAdapter.addLoadStateListener { loadState ->
            binding.progressBar.visibility = if (loadState.refresh is LoadState.Loading) View.VISIBLE else View.GONE

            if (loadState.refresh is LoadState.Error) {
                val errorMessage = (loadState.refresh as LoadState.Error).error.localizedMessage
                Log.d("MainActivity", "Failed to load stories: $errorMessage")
                Toast.makeText(this, "Failed to load stories.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }
}