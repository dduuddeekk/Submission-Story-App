package com.dudek.dicodingstory.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dudek.dicodingstory.database.repositories.StoriesRepository
import com.dudek.dicodingstory.ui.model.MainViewModel

class MainViewModelFactory(
    private val storiesRepository: StoriesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(storiesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}