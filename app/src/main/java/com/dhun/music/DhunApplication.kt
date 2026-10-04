package com.dhun.music

import android.app.Application
import com.dhun.music.database.DhunDatabase
import com.dhun.music.playback.AudioPlaybackManager
import com.dhun.music.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DhunApplication : Application() {

    lateinit var database: DhunDatabase
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var playbackManager: AudioPlaybackManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = DhunDatabase.getInstance(this)
        repository = MusicRepository(this, database.dhunDao())
        playbackManager = AudioPlaybackManager.getInstance(this)

        // Connect playback manager to record recently played
        playbackManager.setOnSongChangeListener { song ->
            repository.recordRecentlyPlayed(song.id)
        }

        // Initialize local music library
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingSongs = repository.allSongs.first()
                if (existingSongs.isEmpty()) {
                    repository.rescanMusic()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        lateinit var instance: DhunApplication
            private set
    }
}
