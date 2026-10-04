package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PlaybackInfo
import com.example.model.Song
import com.example.ui.components.ArtworkView
import com.example.ui.components.DhunEmptyState
import com.example.ui.theme.DhunBackground
import com.example.ui.theme.DhunCard
import com.example.ui.theme.DhunTextSecondary
import com.example.ui.theme.DhunViolet

@Composable
fun QueueScreen(
    playbackInfo: PlaybackInfo,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onClearQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val queue = playbackInfo.queue

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DhunBackground)
            .statusBarsPadding()
            .testTag("queue_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("queue_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.queue_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${queue.size} songs in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = DhunTextSecondary
                    )
                }
            }

            if (queue.isNotEmpty()) {
                TextButton(
                    onClick = onClearQueue,
                    modifier = Modifier.testTag("clear_queue_button")
                ) {
                    Text("Clear", color = DhunViolet)
                }
            }
        }

        if (queue.isEmpty()) {
            DhunEmptyState(
                icon = Icons.Filled.QueueMusic,
                title = stringResource(R.string.empty_queue_title),
                description = stringResource(R.string.empty_queue_desc)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                itemsIndexed(queue) { index, song ->
                    val isCurrent = index == playbackInfo.queueIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSongClick(song) }
                            .background(if (isCurrent) DhunCard else DhunBackground)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .testTag("queue_item_$index"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtworkView(
                            artworkUri = song.albumArtUri,
                            songTitle = song.title,
                            size = 46.dp,
                            shape = RoundedCornerShape(8.dp),
                            iconSize = 20.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) DhunViolet else MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${song.artist} • ${song.formatDuration()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DhunTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onRemoveFromQueue(index) },
                            modifier = Modifier.testTag("remove_queue_item_$index")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove from Queue",
                                tint = DhunTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
