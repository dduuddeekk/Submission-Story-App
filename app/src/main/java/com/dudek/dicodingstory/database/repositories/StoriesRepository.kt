package com.dudek.dicodingstory.database.repositories

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dudek.dicodingstory.data.api.ApiService
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.database.StoriesDatabase
import com.dudek.dicodingstory.database.mediator.StoriesRemoteMediator
import com.dudek.dicodingstory.database.response.StoriesResponseItem

class StoriesRepository(
    private val storiesDatabase: StoriesDatabase,
    private val apiService: ApiService,
    private val sessionPreference: SessionPreference
) {
    fun getStory(): LiveData<PagingData<StoriesResponseItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoriesRemoteMediator(storiesDatabase, apiService, sessionPreference),
            pagingSourceFactory = {
//                StoriesPagingSource(apiService, sessionPreference)
                storiesDatabase.storiesDao().getAllStories()
            }
        ).liveData
    }
}