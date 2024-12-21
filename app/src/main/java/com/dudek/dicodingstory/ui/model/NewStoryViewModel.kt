package com.dudek.dicodingstory.ui.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewStoryViewModel : ViewModel() {

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> get() = _token

    private val _description = MutableLiveData<String>()

    fun setToken(token: String) {
        _token.value = token
    }

    fun setDescription(description: String) {
        _description.value = description
    }
}