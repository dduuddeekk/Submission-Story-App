package com.dudek.dicodingstory.ui.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.response.StoryDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _storyDetail = MutableLiveData<StoryDetail?>()
    val storyDetail: MutableLiveData<StoryDetail?> get() = _storyDetail

    fun fetchStoryDetail(storyId: String, token: String) {
        ApiConfig.getApiService().getStoryDetail("Bearer $token", storyId).enqueue(object : Callback<StoryDetailResponse> {
            override fun onResponse(call: Call<StoryDetailResponse>, response: Response<StoryDetailResponse>) {
                if (response.isSuccessful) {
                    val story = response.body()?.story
                    story?.let {
                        _storyDetail.value = StoryDetail(
                            id = it.id,
                            photoUrl = it.photoUrl,
                            description = it.description
                        )
                    }
                }
            }

            override fun onFailure(call: Call<StoryDetailResponse>, t: Throwable) {
                _storyDetail.value = null
            }
        })
    }
}

data class StoryDetail(
    val id: String,
    val photoUrl: String,
    val description: String
)