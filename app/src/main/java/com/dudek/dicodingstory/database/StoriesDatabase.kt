package com.dudek.dicodingstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dudek.dicodingstory.database.dao.RemoteKeysDao
import com.dudek.dicodingstory.database.dao.StoriesDao
import com.dudek.dicodingstory.database.mediator.RemoteKeys
import com.dudek.dicodingstory.database.response.StoriesResponseItem

@Database(
    entities = [StoriesResponseItem::class, RemoteKeys::class],
    version = 2,
    exportSchema = false
)
abstract class StoriesDatabase : RoomDatabase() {

    abstract fun storiesDao(): StoriesDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoriesDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoriesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoriesDatabase::class.java, "stories_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}