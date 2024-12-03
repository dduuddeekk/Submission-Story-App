package com.dudek.dicodingstory.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dudek.dicodingstory.data.response.ListStoryItem
import com.dudek.dicodingstory.databinding.StoryViewBinding
import com.dudek.dicodingstory.ui.activity.StoryDetailActivity

class StoriesAdapter(
    private val stories: ArrayList<ListStoryItem>,
    private val token: String?
) : RecyclerView.Adapter<StoriesAdapter.StoriesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        val binding = StoryViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) {
        holder.bind(stories[position], token)
    }

    override fun getItemCount(): Int = stories.size

    class StoriesViewHolder(private val binding: StoryViewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: ListStoryItem, token: String?) {
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
}