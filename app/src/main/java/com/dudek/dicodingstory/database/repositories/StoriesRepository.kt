package com.dudek.dicodingstory.database.repositories

import com.dudek.dicodingstory.data.api.ApiService
import com.dudek.dicodingstory.database.StoriesDatabase
import com.dudek.dicodingstory.database.response.StoriesResponseItem

class StoriesRepository(private val storiesDatabase: StoriesDatabase, private val apiService: ApiService) {
    suspend fun getStoriesPage(token: String, page: Int = 1, size: Int = 10): List<StoriesResponseItem> {
        return apiService.getPageStories(
            "Bearer $token",
            page,
            size
        )
    }
}