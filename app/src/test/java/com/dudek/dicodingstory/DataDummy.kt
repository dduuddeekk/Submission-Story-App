package com.dudek.dicodingstory

import com.dudek.dicodingstory.database.response.StoriesResponseItem

object DataDummy {
    fun generateDummyStoriesResponse(): List<StoriesResponseItem> {
        val items: MutableList<StoriesResponseItem> = arrayListOf()
        for (i in 0..100) {
            val story = StoriesResponseItem(
                id = i.toString(),
                name = "name $i",
                description = "desc $i",
                photoUrl = "photoUrl $i",
                createdAt = "2024-12-18T00:00:00Z",
                lat = i.toDouble(),
                lon = i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}