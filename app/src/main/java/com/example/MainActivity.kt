package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.DhunBottomNav
import com.example.ui.components.DhunNavTab
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SongDetailsDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.QueueScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DhunBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DhunViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DhunViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                DhunApp(viewModel)
            }
        }
    }
}

@Composable
fun DhunApp(viewModel: DhunViewModel) {
    val context = LocalContext.current

    // State collections
    val selectedTab by viewModel.selectedTab.collectAsState()
    val playbackInfo by viewModel.playbackInfo.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val showNowPlaying by viewModel.showNowPlaying.collectAsState()
    val showQueue by viewModel.showQueue.collectAsState()
    val activePlaylist by viewModel.activePlaylist.collectAsState()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsState()

    // Dialog States
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var songForDetails by remember { mutableStateOf<Song?>(null) }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        if (audioGranted) {
            viewModel.rescanMusic()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Share action helper
    val onShareSong: (Song) -> Unit = { song ->
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Listening to ${song.title}")
            putExtra(Intent.EXTRA_TEXT, "Listening to \"${song.title}\" by ${song.artist} on Dhun Music Player 🎵")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Music"))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DhunBackground)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DhunBackground,
            bottomBar = {
                Column {
                    // Mini player docked above bottom navigation
                    if (playbackInfo.currentSong != null && !showNowPlaying) {
                        MiniPlayer(
                            playbackInfo = playbackInfo,
                            onOpenNowPlaying = { viewModel.showNowPlaying.value = true },
                            onPlayPause = { viewModel.playOrPause() },
                            onNext = { viewModel.next() }
                        )
                    }

                    DhunBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            viewModel.activePlaylist.value = null
                            viewModel.selectedTab.value = tab
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // If viewing a playlist detail screen
                if (activePlaylist != null) {
                    val playlist = activePlaylist!!
                    val playlistSongs by viewModel.getPlaylistSongs(playlist.id).collectAsState(initial = emptyList())
                    PlaylistDetailScreen(
                        playlist = playlist,
                        songs = playlistSongs,
                        playbackInfo = playbackInfo,
                        onBack = { viewModel.activePlaylist.value = null },
                        onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                        onAddToQueue = { song -> viewModel.addToQueue(song) },
                        onPlayNext = { song -> viewModel.playNext(song) },
                        onRemoveFromPlaylist = { song -> viewModel.removeSongFromPlaylist(playlist.id, song.id) },
                        onDeletePlaylist = { viewModel.deletePlaylist(playlist.id) },
                        onShowDetails = { song -> songForDetails = song },
                        onShare = onShareSong
                    )
                } else {
                    // Primary Navigation Screens
                    when (selectedTab) {
                        DhunNavTab.HOME -> {
                            HomeScreen(
                                allSongs = allSongs,
                                favoriteSongs = favoriteSongs,
                                recentlyPlayed = recentlyPlayed,
                                playlists = playlists,
                                playbackInfo = playbackInfo,
                                onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                                onOpenLibraryTab = { tab -> viewModel.selectedTab.value = tab },
                                onOpenSettings = { viewModel.selectedTab.value = DhunNavTab.SETTINGS },
                                onSelectPlaylist = { playlist -> viewModel.activePlaylist.value = playlist }
                            )
                        }
                        DhunNavTab.SEARCH -> {
                            SearchScreen(
                                searchQuery = searchQuery,
                                onQueryChange = { viewModel.searchQuery.value = it },
                                searchResults = searchResults,
                                allSongs = allSongs,
                                playbackInfo = playbackInfo,
                                onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                                onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                                onAddToQueue = { song -> viewModel.addToQueue(song) },
                                onPlayNext = { song -> viewModel.playNext(song) },
                                onAddToPlaylist = { song -> songForAddToPlaylist = song },
                                onShowDetails = { song -> songForDetails = song },
                                onShare = onShareSong
                            )
                        }
                        DhunNavTab.LIBRARY -> {
                            LibraryScreen(
                                allSongs = allSongs,
                                favoriteSongs = favoriteSongs,
                                playlists = playlists,
                                albums = albums,
                                artists = artists,
                                folders = folders,
                                playbackInfo = playbackInfo,
                                onSongClick = { song, queue -> viewModel.playSong(song, queue) },
                                onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                                onAddToQueue = { song -> viewModel.addToQueue(song) },
                                onPlayNext = { song -> viewModel.playNext(song) },
                                onAddToPlaylist = { song -> songForAddToPlaylist = song },
                                onShowDetails = { song -> songForDetails = song },
                                onShare = onShareSong,
                                onSelectPlaylist = { playlist -> viewModel.activePlaylist.value = playlist },
                                onCreatePlaylist = { showCreatePlaylistDialog = true },
                                onDeletePlaylist = { playlist -> viewModel.deletePlaylist(playlist.id) }
                            )
                        }
                        DhunNavTab.SETTINGS -> {
                            SettingsScreen(
                                isScanning = isScanning,
                                onRescanLibrary = { filterShort -> viewModel.rescanMusic(filterShort) },
                                sleepTimerMinutes = sleepTimerMinutes,
                                onSetSleepTimer = { mins -> viewModel.setSleepTimer(mins) },
                                totalSongCount = allSongs.size
                            )
                        }
                    }
                }
            }
        }

        // Full Screen Now Playing Overlay
        AnimatedVisibility(
            visible = showNowPlaying && playbackInfo.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                playbackInfo = playbackInfo,
                onCollapse = { viewModel.showNowPlaying.value = false },
                onPlayPause = { viewModel.playOrPause() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onSeek = { pos -> viewModel.seekTo(pos) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                onOpenQueue = { viewModel.showQueue.value = true },
                onSetSleepTimer = { mins -> viewModel.setSleepTimer(mins) },
                sleepTimerMinutes = sleepTimerMinutes
            )
        }

        // Full Screen Queue Overlay
        AnimatedVisibility(
            visible = showQueue,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            QueueScreen(
                playbackInfo = playbackInfo,
                onBack = { viewModel.showQueue.value = false },
                onSongClick = { song -> viewModel.playSong(song) },
                onRemoveFromQueue = { index -> viewModel.removeFromQueue(index) },
                onClearQueue = { viewModel.clearQueue() }
            )
        }

        // Dialogs
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { showCreatePlaylistDialog = false },
                onCreate = { name ->
                    viewModel.createPlaylist(name)
                    showCreatePlaylistDialog = false
                }
            )
        }

        songForAddToPlaylist?.let { song ->
            AddToPlaylistDialog(
                song = song,
                playlists = playlists,
                onDismiss = { songForAddToPlaylist = null },
                onPlaylistSelected = { playlistId ->
                    viewModel.addSongToPlaylist(playlistId, song.id)
                    songForAddToPlaylist = null
                },
                onCreateNewPlaylist = {
                    songForAddToPlaylist = null
                    showCreatePlaylistDialog = true
                }
            )
        }

        songForDetails?.let { song ->
            SongDetailsDialog(
                song = song,
                onDismiss = { songForDetails = null }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
