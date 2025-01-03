package com.dudek.dicodingstory.database.di

import android.content.Context
import com.dudek.dicodingstory.data.api.ApiConfig
import com.dudek.dicodingstory.data.pref.SessionPreference
import com.dudek.dicodingstory.database.StoriesDatabase
import com.dudek.dicodingstory.database.repositories.StoriesRepository

object Injection {
    fun provideRepository(context: Context): StoriesRepository {
        val database = StoriesDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        val sessionPreference = SessionPreference(context)

        return StoriesRepository(database, apiService, sessionPreference)
    }
}
