package com.dudek.dicodingstory.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.data.response.ListStoryItem
import com.dudek.dicodingstory.databinding.StoryViewBinding
import com.dudek.dicodingstory.ui.activity.StoryDetailActivity

class StoriesAdapter(
    private val stories: List<ListStoryItem>,
    private val token: String?
) : RecyclerView.Adapter<StoriesAdapter.StoriesViewHolder>() {

    class StoriesViewHolder(private val binding: StoryViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem, token: String?) {
            binding.apply {
                tvStoryTitle.text = story.name
                tvStoryDescription.text = story.description
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(ivStoryCover)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, StoryDetailActivity::class.java).apply {
                    putExtra("EXTRA_STORY_ID", story.id)
                    putExtra("EXTRA_TOKEN", token)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val binding = StoryViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        holder.bind(stories[position], token) // Pass the token when binding the item
    }

    override fun getItemCount(): Int = stories.size
}
