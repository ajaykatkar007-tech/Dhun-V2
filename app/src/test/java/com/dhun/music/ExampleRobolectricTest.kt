package com.dhun.music

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.dhun.music.database.DhunDao
import com.dhun.music.database.DhunDatabase
import com.dhun.music.database.FavoriteEntity
import com.dhun.music.database.PlaylistEntity
import com.dhun.music.database.PlaylistSongEntity
import com.dhun.music.database.SongEntity
import com.dhun.music.model.PlaybackInfo
import com.dhun.music.model.PlayerStatus
import com.dhun.music.model.RepeatMode
import com.dhun.music.model.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: DhunDatabase
  private lateinit var dao: DhunDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, DhunDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.dhunDao()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dhun", appName)
  }

  @Test
  fun `song formatting works correctly`() {
    val song = Song(
      id = 1L,
      title = "Raag Bhairav",
      artist = "Pt. Hariprasad",
      album = "Classical Dawn",
      durationMs = 185000L,
      contentUri = "content://media/external/audio/1"
    )
    assertEquals("3:05", song.formatDuration())
  }

  @Test
  fun `playback progress calculation`() {
    val song = Song(
      id = 1L,
      title = "Monsoon Beats",
      artist = "Dhun Collective",
      album = "Indian Rain",
      durationMs = 200000L,
      contentUri = "content://media/external/audio/2"
    )
    val info = PlaybackInfo(
      currentSong = song,
      status = PlayerStatus.PLAYING,
      currentPositionMs = 100000L,
      durationMs = 200000L,
      repeatMode = RepeatMode.ALL,
      isShuffle = true
    )
    assertTrue(info.isPlaying)
    assertEquals(0.5f, info.progress, 0.01f)
    assertEquals("1:40", info.formatCurrentPosition())
    assertEquals("3:20", info.formatDuration())
  }

  @Test
  fun `insert and retrieve songs in dao`() = runBlocking {
    val song = SongEntity(
      id = 101L,
      title = "Sitar Echoes",
      artist = "Ravi Tribute",
      album = "Varanasi Chill",
      durationMs = 150000L,
      contentUri = "content://audio/101",
      albumArtUri = null,
      folder = "Classical",
      dateAdded = System.currentTimeMillis()
    )
    dao.insertSongs(listOf(song))

    val retrieved = dao.getSongById(101L)
    assertEquals("Sitar Echoes", retrieved?.title)
    assertEquals("Ravi Tribute", retrieved?.artist)

    val all = dao.getAllSongs().first()
    assertEquals(1, all.size)
  }

  @Test
  fun `favorites insert and delete`() = runBlocking {
    dao.insertFavorite(FavoriteEntity(songId = 101L))
    val isFav = dao.isFavorite(101L).first()
    assertTrue(isFav)

    dao.deleteFavorite(101L)
    val isFavAfter = dao.isFavorite(101L).first()
    assertFalse(isFavAfter)
  }

  @Test
  fun `playlist operations`() = runBlocking {
    val playlistId = dao.insertPlaylist(PlaylistEntity(name = "Evening Ragas"))
    val playlists = dao.getAllPlaylists().first()
    assertEquals(1, playlists.size)
    assertEquals("Evening Ragas", playlists[0].name)

    dao.insertPlaylistSong(PlaylistSongEntity(playlistId = playlistId, songId = 101L, orderIndex = 0))
    val playlistSongs = dao.getPlaylistSongs(playlistId).first()
    assertEquals(1, playlistSongs.size)
    assertEquals(101L, playlistSongs[0].songId)

    dao.removeSongFromPlaylist(playlistId, 101L)
    val afterRemove = dao.getPlaylistSongs(playlistId).first()
    assertTrue(afterRemove.isEmpty())

    dao.deletePlaylist(playlistId)
    val afterDelete = dao.getAllPlaylists().first()
    assertTrue(afterDelete.isEmpty())
  }
}
