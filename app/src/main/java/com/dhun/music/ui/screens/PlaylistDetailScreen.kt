package com.dhun.music.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dhun.music.model.PlaybackInfo
import com.dhun.music.model.Playlist
import com.dhun.music.model.Song
import com.dhun.music.ui.components.DhunEmptyState
import com.dhun.music.ui.components.SongRowItem
import com.dhun.music.ui.theme.DhunPink
import com.dhun.music.ui.theme.DhunTextSecondary
import com.dhun.music.ui.theme.DhunViolet
import com.dhun.music.ui.theme.DhunVioletDark

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    songs: List<Song>,
    playbackInfo: PlaybackInfo,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onRemoveFromPlaylist: (Song) -> Unit,
    onDeletePlaylist: () -> Unit,
    onShowDetails: (Song) -> Unit,
    onShare: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurationMs = songs.sumOf { it.durationMs }
    val totalMinutes = totalDurationMs / 60000

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("playlist_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onDeletePlaylist,
                modifier = Modifier.testTag("playlist_delete_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Playlist",
                    tint = DhunTextSecondary
                )
            }
        }

        // Header Hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(DhunViolet, DhunVioletDark, DhunPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QueueMusic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = playlist.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${songs.size} songs • $totalMinutes mins",
                style = MaterialTheme.typography.bodyMedium,
                color = DhunTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (songs.isNotEmpty()) {
                            onSongClick(songs.first(), songs)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("playlist_play_all_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DhunViolet)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play")
                }

                OutlinedButton(
                    onClick = {
                        if (songs.isNotEmpty()) {
                            val shuffled = songs.shuffled()
                            onSongClick(shuffled.first(), shuffled)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("playlist_shuffle_button")
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp), tint = DhunPink)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Song List
        if (songs.isEmpty()) {
            DhunEmptyState(
                icon = Icons.Filled.QueueMusic,
                title = "Playlist is Empty",
                description = "Add songs to this playlist from the Library or Search screen."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(songs, key = { it.id }) { song ->
                    SongRowItem(
                        song = song,
                        isPlaying = playbackInfo.isPlaying && song.id == playbackInfo.currentSong?.id,
                        isCurrentTrack = song.id == playbackInfo.currentSong?.id,
                        onClick = { onSongClick(song, songs) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onPlayNext = { onPlayNext(song) },
                        onAddToPlaylist = {},
                        onShowDetails = { onShowDetails(song) },
                        onShare = { onShare(song) }
                    )
                }
            }
        }
    }
}
