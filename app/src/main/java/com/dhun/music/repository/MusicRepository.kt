package com.dhun.music.repository

import android.content.Context
import com.dhun.music.database.DhunDao
import com.dhun.music.database.FavoriteEntity
import com.dhun.music.database.PlaylistEntity
import com.dhun.music.database.PlaylistSongEntity
import com.dhun.music.database.RecentlyPlayedEntity
import com.dhun.music.database.SongEntity
import com.dhun.music.model.Album
import com.dhun.music.model.Artist
import com.dhun.music.model.Folder
import com.dhun.music.model.Playlist
import com.dhun.music.model.Song
import com.dhun.music.scanner.MediaScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val dao: DhunDao
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val allSongs: Flow<List<Song>> = combine(
        dao.getAllSongs(),
        dao.getAllFavorites()
    ) { entities, favorites ->
        val favoriteIds = favorites.map { it.songId }.toSet()
        entities.map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                album = entity.album,
                durationMs = entity.durationMs,
                contentUri = entity.contentUri,
                albumArtUri = entity.albumArtUri,
                folder = entity.folder,
                dateAdded = entity.dateAdded,
                isFavorite = favoriteIds.contains(entity.id),
                isLocalSample = entity.isLocalSample
            )
        }
    }

    val favoriteSongs: Flow<List<Song>> = allSongs.map { songs ->
        songs.filter { it.isFavorite }
    }

    val recentlyPlayedSongs: Flow<List<Song>> = combine(
        allSongs,
        dao.getRecentlyPlayed()
    ) { songs, recentEntries ->
        val songMap = songs.associateBy { it.id }
        recentEntries.mapNotNull { recent -> songMap[recent.songId] }
    }

    val playlists: Flow<List<Playlist>> = dao.getAllPlaylists().map { entities ->
        entities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt
            )
        }
    }

    val albums: Flow<List<Album>> = allSongs.map { songs ->
        songs.groupBy { it.album }
            .map { (albumName, albumSongs) ->
                Album(
                    name = albumName,
                    artist = albumSongs.firstOrNull()?.artist ?: "Various Artists",
                    songCount = albumSongs.size,
                    albumArtUri = albumSongs.firstOrNull { it.albumArtUri != null }?.albumArtUri
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    val artists: Flow<List<Artist>> = allSongs.map { songs ->
        songs.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                val albumCount = artistSongs.map { it.album }.distinct().size
                Artist(
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = albumCount
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    val folders: Flow<List<Folder>> = allSongs.map { songs ->
        songs.groupBy { it.folder }
            .map { (folderName, folderSongs) ->
                Folder(
                    name = folderName,
                    path = folderName,
                    songCount = folderSongs.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun rescanMusic(ignoreShortAudio: Boolean = true) = withContext(Dispatchers.IO) {
        val minDuration = if (ignoreShortAudio) 30_000L else 5_000L
        val scanned = MediaScanner.scanDeviceAudio(context, minDuration)

        val entities = scanned.map { song ->
            SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                durationMs = song.durationMs,
                contentUri = song.contentUri,
                albumArtUri = song.albumArtUri,
                folder = song.folder,
                dateAdded = song.dateAdded,
                isLocalSample = song.isLocalSample
            )
        }

        dao.clearScannedSongs()
        dao.insertSongs(entities)
    }

    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val isFav = dao.isFavorite(songId).first()
        if (isFav) {
            dao.deleteFavorite(songId)
        } else {
            dao.insertFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        dao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        dao.updatePlaylistName(playlistId, newName)
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        dao.deletePlaylist(playlistId)
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return combine(allSongs, dao.getPlaylistSongs(playlistId)) { songs, playlistSongs ->
            val songMap = songs.associateBy { it.id }
            playlistSongs.mapNotNull { ps -> songMap[ps.songId] }
        }
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        val currentCount = dao.getPlaylistSongs(playlistId).first().size
        dao.insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                orderIndex = currentCount
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    fun recordRecentlyPlayed(songId: Long) {
        repositoryScope.launch {
            dao.insertRecentlyPlayed(RecentlyPlayedEntity(songId = songId))
        }
    }
}
