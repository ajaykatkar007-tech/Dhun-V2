package com.dhun.music.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhun.music.DhunApplication
import com.dhun.music.model.Album
import com.dhun.music.model.Artist
import com.dhun.music.model.Folder
import com.dhun.music.model.PlaybackInfo
import com.dhun.music.model.Playlist
import com.dhun.music.model.Song
import com.dhun.music.scanner.MusicScanResult
import com.dhun.music.ui.components.DhunNavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DhunViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = DhunApplication.instance.repository
    private val playbackManager = DhunApplication.instance.playbackManager

    val playbackInfo: StateFlow<PlaybackInfo> = playbackManager.playbackInfo

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedSongs: StateFlow<List<Song>> = repository.recentlyPlayedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = repository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = repository.artists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<Folder>> = repository.folders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    val selectedTab = MutableStateFlow(DhunNavTab.HOME)
    val searchQuery = MutableStateFlow("")

    val showNowPlaying = MutableStateFlow(false)
    val showQueue = MutableStateFlow(false)
    val activePlaylist = MutableStateFlow<Playlist?>(null)

    private data class NavigationEntry(
        val tab: DhunNavTab,
        val playlistId: Long? = null,
        val overlay: String = "ROOT"
    ) {
        fun encode(): String = "${tab.name}|${playlistId ?: "-"}|$overlay"

        companion object {
            fun decode(value: String): NavigationEntry? {
                val parts = value.split("|")
                if (parts.size != 3) return null
                val tab = runCatching { DhunNavTab.valueOf(parts[0]) }.getOrNull() ?: return null
                val playlistId = parts[1].takeIf { it != "-" }?.toLongOrNull()
                return NavigationEntry(tab, playlistId, parts[2])
            }
        }
    }

    private val navigationKey = "dhun_navigation_stack"
    private val _navigationStack = MutableStateFlow(
        (savedStateHandle.get<ArrayList<String>>(navigationKey) ?: arrayListOf("HOME|-|ROOT"))
            .mapNotNull(NavigationEntry::decode)
            .ifEmpty { listOf(NavigationEntry(DhunNavTab.HOME)) }
    )

    private fun persistNavigation() {
        savedStateHandle[navigationKey] = ArrayList(_navigationStack.value.map { it.encode() })
    }

    private fun syncLegacyNavigationState() {
        val entry = _navigationStack.value.lastOrNull() ?: NavigationEntry(DhunNavTab.HOME)
        selectedTab.value = entry.tab
        showQueue.value = entry.overlay == "QUEUE"
        showNowPlaying.value = entry.overlay == "PLAYER"
        activePlaylist.value = entry.playlistId?.let { id -> playlists.value.firstOrNull { it.id == id } }
    }

    fun selectTab(tab: DhunNavTab) {
        _navigationStack.value = listOf(NavigationEntry(tab))
        persistNavigation()
        syncLegacyNavigationState()
    }

    fun openPlaylist(playlist: Playlist) {
        val current = _navigationStack.value.lastOrNull() ?: NavigationEntry(selectedTab.value)
        _navigationStack.value = _navigationStack.value.dropLast(1) +
            current.copy(overlay = "ROOT") +
            NavigationEntry(current.tab, playlist.id, "ROOT")
        persistNavigation()
        syncLegacyNavigationState()
    }

    fun openQueue() {
        val current = _navigationStack.value.lastOrNull() ?: NavigationEntry(selectedTab.value)
        if (current.overlay == "QUEUE") return
        _navigationStack.value = _navigationStack.value + current.copy(overlay = "QUEUE")
        persistNavigation()
        syncLegacyNavigationState()
    }

    fun openNowPlaying() {
        val current = _navigationStack.value.lastOrNull() ?: NavigationEntry(selectedTab.value)
        if (current.overlay == "PLAYER") return
        _navigationStack.value = _navigationStack.value + current.copy(overlay = "PLAYER")
        persistNavigation()
        syncLegacyNavigationState()
    }

    fun navigateBack(): Boolean {
        if (_navigationStack.value.size <= 1) return false
        _navigationStack.value = _navigationStack.value.dropLast(1)
        persistNavigation()
        syncLegacyNavigationState()
        return true
    }

    init {
        syncLegacyNavigationState()
        viewModelScope.launch {
            playlists.collect { syncLegacyNavigationState() }
        }
        // Automatically synchronize favorites state with currently playing song and active queue
        viewModelScope.launch {
            repository.favoriteSongs.collect { favs ->
                val favIds = favs.map { it.id }.toSet()
                val current = playbackManager.playbackInfo.value.currentSong
                if (current != null) {
                    val shouldBeFav = favIds.contains(current.id)
                    if (current.isFavorite != shouldBeFav) {
                        playbackManager.updateSongFavorite(current.id, shouldBeFav)
                    }
                }
            }
        }
    }

    // Filtered search results
    val searchResults: StateFlow<List<Song>> = combine(
        allSongs,
        searchQuery
    ) { songs, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            songs.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q) ||
                it.folder.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Playback Actions ---
    fun playSong(song: Song, queue: List<Song>? = null) {
        playbackManager.playSong(song, queue)
    }

    fun playOrPause() {
        playbackManager.playOrPause()
    }

    fun next() {
        playbackManager.next()
    }

    fun previous() {
        playbackManager.previous()
    }

    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playbackManager.toggleRepeat()
    }

    fun addToQueue(song: Song) {
        playbackManager.addToQueue(song)
    }

    fun playNext(song: Song) {
        playbackManager.playNext(song)
    }

    fun removeFromQueue(index: Int) {
        playbackManager.removeFromQueue(index)
    }

    fun clearQueue() {
        playbackManager.clearQueue()
    }

    // --- Library Actions ---
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (activePlaylist.value?.id == playlistId) {
                navigateBack()
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistSongs(playlistId: Long) = repository.getPlaylistSongs(playlistId)

    fun rescanMusic(ignoreShortAudio: Boolean = true) {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _scanMessage.value = "Scanning your music library…"
            try {
                val report = repository.rescanMusic(ignoreShortAudio)
                _scanMessage.value = when (val result = report.result) {
                    is MusicScanResult.Success -> if (report.newlyFoundCount > 0) {
                        "Scan complete • ${report.newlyFoundCount} new track(s) found"
                    } else {
                        "Scan complete • No new MP3 found"
                    }
                    is MusicScanResult.PartialSuccess ->
                        "Partial scan • ${report.newlyFoundCount} new track(s) found; ${result.skippedCount} item(s) could not be read"
                    is MusicScanResult.Empty -> "No new MP3 found • Your existing library was preserved"
                    MusicScanResult.PermissionDenied -> "Storage permission required. Open app settings to allow audio access."
                    is MusicScanResult.Failed -> "Scan failed • ${result.reason}. Your existing library was preserved"
                    MusicScanResult.Cancelled -> "Scan cancelled • Your existing library was preserved"
                }
            } catch (error: Exception) {
                _scanMessage.value = "Scan failed • ${error.message ?: "Unknown error"}. Your existing library was preserved"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun checkForLibraryChanges() {
        if (_isScanning.value) return
        viewModelScope.launch {
            if (repository.hasPotentialLibraryChanges()) {
                _scanMessage.value = "New audio may be available • Tap Scan to refresh"
            }
        }
    }
}
