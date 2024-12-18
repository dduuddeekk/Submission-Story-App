package com.dudek.dicodingstory.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dudek.dicodingstory.database.response.StoriesResponseItem

@Dao
interface StoriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(stories: List<StoriesResponseItem>)

    @Query("SELECT * FROM stories")
    fun getAllStories(): PagingSource<Int, StoriesResponseItem>

    @Query("DELETE FROM stories")
    suspend fun deleteAll()
}