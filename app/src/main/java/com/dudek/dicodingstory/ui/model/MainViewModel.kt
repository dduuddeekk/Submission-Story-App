package com.dudek.dicodingstory.ui.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dudek.dicodingstory.database.repositories.StoriesRepository
import com.dudek.dicodingstory.database.response.StoriesResponseItem

class MainViewModel(storiesRepository: StoriesRepository) : ViewModel() {

    val stories: LiveData<PagingData<StoriesResponseItem>> =
        storiesRepository.getStory().cachedIn(viewModelScope)
}