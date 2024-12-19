package com.dudek.dicodingstory.ui.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dudek.dicodingstory.data.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _storyDetail = MutableLiveData<StoryDetail?>()
    val storyDetail: LiveData<StoryDetail?> get() = _storyDetail

    fun fetchStoryDetail(storyId: String, token: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().getStoryDetail("Bearer $token", storyId)
                }

                if (response.error == false) {
                    response.story?.let { story ->
                        _storyDetail.value = StoryDetail(
                            id = story.id.orEmpty(),
                            photoUrl = story.photoUrl.orEmpty(),
                            name = story.name.orEmpty(),
                            description = story.description.orEmpty()
                        )
                    } ?: run {
                        _storyDetail.value = null
                    }
                } else {
                    _storyDetail.value = null
                }
            } catch (e: Exception) {
                _storyDetail.value = null
            }
        }
    }
}

data class StoryDetail(
    val id: String,
    val photoUrl: String,
    val name: String,
    val description: String
)