package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PlaybackInfo
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.ui.components.ArtworkView
import com.example.ui.theme.DhunBackground
import com.example.ui.theme.DhunBorder
import com.example.ui.theme.DhunPink
import com.example.ui.theme.DhunTextSecondary
import com.example.ui.theme.DhunViolet
import com.example.ui.theme.DhunVioletDark

@Composable
fun NowPlayingScreen(
    playbackInfo: PlaybackInfo,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenQueue: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    sleepTimerMinutes: Int,
    modifier: Modifier = Modifier
) {
    val currentSong = playbackInfo.currentSong ?: return
    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragSliderValue by remember { mutableFloatStateOf(0f) }

    val sliderPosition = if (isDraggingSlider) {
        dragSliderValue
    } else {
        if (playbackInfo.durationMs > 0) {
            (playbackInfo.currentPositionMs.toFloat() / playbackInfo.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DhunBackground)
    ) {
        // Ambient glow backdrop derived from brand colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DhunViolet.copy(alpha = 0.18f),
                            DhunPink.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 1100f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .testTag("now_playing_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.testTag("collapse_now_playing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.now_playing_header),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DhunViolet
                    )
                    Text(
                        text = currentSong.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = DhunTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier.testTag("now_playing_queue_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Large Artwork Card with Ambient Glow & Hairline Border ---
            Box(
                modifier = Modifier.size(310.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ambient color halo behind artwork
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(DhunViolet.copy(alpha = 0.45f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                )

                // Artwork container with subtle hairline border and elevation
                Box(
                    modifier = Modifier
                        .size(290.dp)
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp), spotColor = DhunVioletDark)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, DhunBorder.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ArtworkView(
                        artworkUri = currentSong.albumArtUri,
                        songTitle = currentSong.title,
                        size = 290.dp,
                        shape = RoundedCornerShape(24.dp),
                        iconSize = 96.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Song Title, Artist & Like Button ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = DhunTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onToggleFavorite(currentSong) },
                    modifier = Modifier.testTag("now_playing_favorite_button")
                ) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (currentSong.isFavorite) "Liked" else "Like",
                        tint = if (currentSong.isFavorite) DhunPink else DhunTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Custom Refined Seek Bar with Violet-to-Pink Gradient Track ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("playback_seek_bar"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Inactive background track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DhunBorder)
                    )
                    // Active gradient track (Violet to Pink)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = sliderPosition.coerceIn(0f, 1f))
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(DhunViolet, DhunPink)
                                )
                            )
                    )
                    // Thumb indicator
                    Box(
                        modifier = Modifier
                            .padding(
                                start = ((sliderPosition * 100).coerceIn(0f, 100f) * 0.01f * 260).dp
                            )
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(DhunPink)
                            .border(2.dp, Color.White, CircleShape)
                    )
                    // Invisible native slider overlay for interaction
                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            isDraggingSlider = true
                            dragSliderValue = it
                        },
                        onValueChangeFinished = {
                            val targetMs = (dragSliderValue * playbackInfo.durationMs).toLong()
                            onSeek(targetMs)
                            isDraggingSlider = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Transparent,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isDraggingSlider) {
                            val posMs = (dragSliderValue * playbackInfo.durationMs).toLong()
                            val s = (posMs / 1000) % 60
                            val m = (posMs / 1000) / 60
                            String.format("%d:%02d", m, s)
                        } else playbackInfo.formatCurrentPosition(),
                        style = MaterialTheme.typography.labelMedium,
                        color = DhunTextSecondary
                    )
                    Text(
                        text = playbackInfo.formatDuration(),
                        style = MaterialTheme.typography.labelMedium,
                        color = DhunTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Primary Controls with Gradient Glow Play Button ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("now_playing_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackInfo.isShuffle) DhunViolet else DhunTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.testTag("now_playing_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause (Distinctive Dhun Gradient Button with outer halo)
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft outer glow halo
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(DhunViolet.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                    // Primary gradient circle button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DhunViolet, DhunPink)
                                )
                            )
                            .testTag("now_playing_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier.size(68.dp)
                        ) {
                            Icon(
                                imageVector = if (playbackInfo.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackInfo.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Next
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("now_playing_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat (Off / All / One)
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.testTag("now_playing_repeat_button")
                ) {
                    Icon(
                        imageVector = when (playbackInfo.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (playbackInfo.repeatMode != RepeatMode.OFF) DhunPink else DhunTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Bottom Controls ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenQueue,
                    modifier = Modifier.testTag("now_playing_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
