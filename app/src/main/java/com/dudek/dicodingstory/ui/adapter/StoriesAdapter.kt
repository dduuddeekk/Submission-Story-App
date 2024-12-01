package com.dudek.dicodingstory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.data.response.ListStoryItem
import com.dudek.dicodingstory.databinding.StoryViewBinding

class StoriesAdapter(private val stories: List<ListStoryItem>) :
    RecyclerView.Adapter<StoriesAdapter.StoriesViewHolder>() {

    class StoriesViewHolder(private val binding: StoryViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.apply {
                tvStoryTitle.text = story.name
                tvStoryDescription.text = story.description
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(ivStoryCover)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val binding = StoryViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

}