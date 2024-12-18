package com.dudek.dicodingstory.ui.model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewStoryViewModel : ViewModel() {

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> get() = _token

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    private val _description = MutableLiveData<String>()

    fun setToken(token: String) {
        _token.value = token
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun setDescription(description: String) {
        _description.value = description
    }
}
