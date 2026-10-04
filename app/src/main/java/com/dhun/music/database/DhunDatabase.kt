package com.dhun.music.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        RecentlyPlayedEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DhunDatabase : RoomDatabase() {
    abstract fun dhunDao(): DhunDao

    companion object {
        @Volatile
        private var INSTANCE: DhunDatabase? = null

        fun getInstance(context: Context): DhunDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DhunDatabase::class.java,
                    "dhun_music_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
