package com.dudek.dicodingstory.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.databinding.ActivityStoryDetailBinding
import com.dudek.dicodingstory.ui.model.StoryDetailViewModel

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailBinding
    private val storyDetailViewModel: StoryDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyId = intent.getStringExtra("EXTRA_STORY_ID")
        val token = intent.getStringExtra("EXTRA_TOKEN")

        binding.apply {
            progressBar.visibility = View.VISIBLE
            ivStoryCover.visibility = View.GONE
            tvUserStory.visibility = View.GONE
            tvStoryDescription.visibility = View.GONE
        }

        if (storyId != null && token != null) {
            storyDetailViewModel.fetchStoryDetail(storyId, token)

            storyDetailViewModel.storyDetail.observe(this) { story ->
                if (story != null) {
                    binding.apply {
                        progressBar.visibility = View.GONE
                        ivStoryCover.visibility = View.VISIBLE
                        tvUserStory.visibility = View.VISIBLE
                        tvStoryDescription.visibility = View.VISIBLE
                        tvUserStory.text = story.name
                        tvStoryDescription.text = story.description
                        Glide.with(this@StoryDetailActivity)
                            .load(story.photoUrl)
                            .into(ivStoryCover)
                    }
                } else {
                    Toast.makeText(this, "Failed to load story details", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Invalid story data", Toast.LENGTH_SHORT).show()
        }
    }
}