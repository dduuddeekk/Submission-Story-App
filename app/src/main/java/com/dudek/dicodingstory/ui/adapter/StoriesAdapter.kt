package com.dudek.dicodingstory.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.databinding.StoryViewBinding
import com.dudek.dicodingstory.ui.activity.StoryDetailActivity
import androidx.core.util.Pair
import com.dudek.dicodingstory.database.response.StoriesResponseItem

class StoriesAdapter(
    private val token: String?
) : PagingDataAdapter<StoriesResponseItem, StoriesAdapter.StoriesViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val binding = StoryViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story, token)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    class StoriesViewHolder(private val binding: StoryViewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: StoriesResponseItem, token: String?) {
            with(binding) {
                tvStoryTitle.text = story.name
                tvStoryDescription.text = story.description

                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(ivStoryCover)

                root.setOnClickListener {
                    val intent = Intent(root.context, StoryDetailActivity::class.java).apply {
                        putExtra("EXTRA_STORY_ID", story.id)
                        putExtra("EXTRA_TOKEN", token)
                    }

                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            root.context as Activity,
                            Pair(ivStoryCover, "cover"),
                            Pair(tvStoryTitle, "description")
                        )
                    root.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoriesResponseItem>() {
            override fun areItemsTheSame(oldItem: StoriesResponseItem, newItem: StoriesResponseItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoriesResponseItem, newItem: StoriesResponseItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}