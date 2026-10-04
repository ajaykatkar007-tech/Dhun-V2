package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Folder
import com.example.model.PlaybackInfo
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.ArtworkView
import com.example.ui.components.DhunEmptyState
import com.example.ui.components.SongRowItem
import com.example.ui.theme.DhunAmber
import com.example.ui.theme.DhunBackground
import com.example.ui.theme.DhunCard
import com.example.ui.theme.DhunPink
import com.example.ui.theme.DhunPinkDeep
import com.example.ui.theme.DhunSurface
import com.example.ui.theme.DhunTextSecondary
import com.example.ui.theme.DhunViolet
import com.example.ui.theme.DhunVioletDark

enum class LibraryTab(val titleRes: Int) {
    SONGS(R.string.tab_songs),
    PLAYLISTS(R.string.tab_playlists),
    ALBUMS(R.string.tab_albums),
    ARTISTS(R.string.tab_artists),
    FOLDERS(R.string.tab_folders)
}

enum class SongSortOption(val label: String) {
    TITLE_ASC("Title (A-Z)"),
    ARTIST_ASC("Artist (A-Z)"),
    DATE_ADDED_DESC("Recently Added"),
    DURATION_DESC("Duration (Longest)")
}

@Composable
fun LibraryScreen(
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    playlists: List<Playlist>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<Folder>,
    playbackInfo: PlaybackInfo,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowDetails: (Song) -> Unit,
    onShare: (Song) -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var sortOption by remember { mutableStateOf(SongSortOption.TITLE_ASC) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val sortedSongs = remember(allSongs, sortOption) {
        when (sortOption) {
            SongSortOption.TITLE_ASC -> allSongs.sortedBy { it.title.lowercase() }
            SongSortOption.ARTIST_ASC -> allSongs.sortedBy { it.artist.lowercase() }
            SongSortOption.DATE_ADDED_DESC -> allSongs.sortedByDescending { it.dateAdded }
            SongSortOption.DURATION_DESC -> allSongs.sortedByDescending { it.durationMs }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Library Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.library_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Sort & Actions
                if (selectedTabIndex == 0) {
                    Box {
                        IconButton(
                            onClick = { sortMenuExpanded = true },
                            modifier = Modifier.testTag("sort_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort Songs",
                                tint = DhunViolet
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            SongSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (sortOption == option) DhunViolet else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        sortOption = option
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DhunBackground,
                contentColor = DhunViolet,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = DhunPink,
                        height = 2.5.dp
                    )
                },
                divider = {}
            ) {
                LibraryTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = stringResource(tab.titleRes),
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) DhunPink else DhunTextSecondary
                            )
                        },
                        modifier = Modifier.testTag("library_tab_${tab.name.lowercase()}")
                    )
                }
            }

            // Tab Content
            when (LibraryTab.entries[selectedTabIndex]) {
                LibraryTab.SONGS -> {
                    if (sortedSongs.isEmpty()) {
                        DhunEmptyState(
                            icon = Icons.Outlined.LibraryMusic,
                            title = stringResource(R.string.empty_library_title),
                            description = stringResource(R.string.empty_library_desc)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${sortedSongs.size} Songs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DhunTextSecondary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = {
                                                if (sortedSongs.isNotEmpty()) {
                                                    onSongClick(sortedSongs.first(), sortedSongs)
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play all", tint = DhunViolet)
                                        }
                                        IconButton(
                                            onClick = {
                                                if (sortedSongs.isNotEmpty()) {
                                                    val shuffled = sortedSongs.shuffled()
                                                    onSongClick(shuffled.first(), shuffled)
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle all", tint = DhunPink)
                                        }
                                    }
                                }
                            }

                            items(sortedSongs, key = { it.id }) { song ->
                                SongRowItem(
                                    song = song,
                                    isPlaying = playbackInfo.isPlaying && song.id == playbackInfo.currentSong?.id,
                                    isCurrentTrack = song.id == playbackInfo.currentSong?.id,
                                    onClick = { onSongClick(song, sortedSongs) },
                                    onToggleFavorite = { onToggleFavorite(song) },
                                    onAddToQueue = { onAddToQueue(song) },
                                    onPlayNext = { onPlayNext(song) },
                                    onAddToPlaylist = { onAddToPlaylist(song) },
                                    onShowDetails = { onShowDetails(song) },
                                    onShare = { onShare(song) }
                                )
                            }
                        }
                    }
                }

                LibraryTab.PLAYLISTS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Built-in Favorites Playlist
                        item {
                            PlaylistItemRow(
                                title = "Liked Songs",
                                subtitle = "${favoriteSongs.size} songs",
                                icon = Icons.Filled.Favorite,
                                gradient = listOf(DhunPink, DhunPinkDeep),
                                onClick = {
                                    if (favoriteSongs.isNotEmpty()) {
                                        onSongClick(favoriteSongs.first(), favoriteSongs)
                                    }
                                },
                                onDelete = null
                            )
                        }

                        // User Playlists
                        items(playlists, key = { it.id }) { playlist ->
                            PlaylistItemRow(
                                title = playlist.name,
                                subtitle = "Playlist",
                                icon = Icons.Filled.QueueMusic,
                                gradient = listOf(DhunViolet, DhunVioletDark),
                                onClick = { onSelectPlaylist(playlist) },
                                onDelete = { onDeletePlaylist(playlist) }
                            )
                        }

                        if (playlists.isEmpty()) {
                            item {
                                DhunEmptyState(
                                    icon = Icons.Filled.QueueMusic,
                                    title = stringResource(R.string.empty_playlists_title),
                                    description = stringResource(R.string.empty_playlists_desc),
                                    actionLabel = stringResource(R.string.create_playlist),
                                    onActionClick = onCreatePlaylist
                                )
                            }
                        }
                    }
                }

                LibraryTab.ALBUMS -> {
                    if (albums.isEmpty()) {
                        DhunEmptyState(
                            icon = Icons.Outlined.Album,
                            title = "No Albums",
                            description = "Scanned audio albums will appear here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(albums) { album ->
                                AlbumItemRow(
                                    album = album,
                                    onClick = {
                                        val albumSongs = allSongs.filter { it.album == album.name }
                                        if (albumSongs.isNotEmpty()) {
                                            onSongClick(albumSongs.first(), albumSongs)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                LibraryTab.ARTISTS -> {
                    if (artists.isEmpty()) {
                        DhunEmptyState(
                            icon = Icons.Outlined.Person,
                            title = "No Artists",
                            description = "Audio artists will appear here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(artists) { artist ->
                                ArtistItemRow(
                                    artist = artist,
                                    onClick = {
                                        val artistSongs = allSongs.filter { it.artist == artist.name }
                                        if (artistSongs.isNotEmpty()) {
                                            onSongClick(artistSongs.first(), artistSongs)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                LibraryTab.FOLDERS -> {
                    if (folders.isEmpty()) {
                        DhunEmptyState(
                            icon = Icons.Outlined.Folder,
                            title = "No Folders",
                            description = "Music storage folders will appear here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(folders) { folder ->
                                FolderItemRow(
                                    folder = folder,
                                    onClick = {
                                        val folderSongs = allSongs.filter { it.folder == folder.name }
                                        if (folderSongs.isNotEmpty()) {
                                            onSongClick(folderSongs.first(), folderSongs)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button for Create Playlist when on Playlists tab
        if (selectedTabIndex == 1) {
            FloatingActionButton(
                onClick = onCreatePlaylist,
                containerColor = DhunViolet,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 100.dp)
                    .testTag("fab_create_playlist")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Playlist")
            }
        }
    }
}

@Composable
private fun PlaylistItemRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DhunTextSecondary
            )
        }

        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Playlist",
                    tint = DhunTextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AlbumItemRow(album: Album, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkView(
            artworkUri = album.albumArtUri,
            songTitle = album.name,
            size = 52.dp,
            shape = RoundedCornerShape(10.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = album.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${album.artist} • ${album.songCount} tracks",
                style = MaterialTheme.typography.bodySmall,
                color = DhunTextSecondary
            )
        }
    }
}

@Composable
private fun ArtistItemRow(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(DhunCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = DhunViolet, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${artist.songCount} songs • ${artist.albumCount} albums",
                style = MaterialTheme.typography.bodySmall,
                color = DhunTextSecondary
            )
        }
    }
}

@Composable
private fun FolderItemRow(folder: Folder, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DhunCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Folder, contentDescription = null, tint = DhunAmber, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${folder.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = DhunTextSecondary
            )
        }
    }
}
