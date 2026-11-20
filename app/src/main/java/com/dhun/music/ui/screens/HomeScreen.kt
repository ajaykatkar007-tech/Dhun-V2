package com.dhun.music.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dhun.music.R
import com.dhun.music.model.PlaybackInfo
import com.dhun.music.model.Playlist
import com.dhun.music.model.Song
import com.dhun.music.ui.components.ArtworkView
import com.dhun.music.ui.components.DhunBrandHeader
import com.dhun.music.ui.components.DhunEmptyState
import com.dhun.music.ui.components.DhunNavTab
import com.dhun.music.ui.components.QuickAccessCard
import com.dhun.music.ui.theme.DhunAmber
import com.dhun.music.ui.theme.DhunBorder
import com.dhun.music.ui.theme.DhunCard
import com.dhun.music.ui.theme.DhunCardElevated
import com.dhun.music.ui.theme.DhunPink
import com.dhun.music.ui.theme.DhunPinkDeep
import com.dhun.music.ui.theme.DhunSuccess
import com.dhun.music.ui.theme.DhunTextPrimary
import com.dhun.music.ui.theme.DhunTextSecondary
import com.dhun.music.ui.theme.DhunViolet
import com.dhun.music.ui.theme.DhunVioletDark
import java.util.Calendar

@Composable
fun HomeScreen(
    allSongs: List<Song>,
    favoriteSongs: List<Song>,
    recentlyPlayed: List<Song>,
    playlists: List<Playlist>,
    playbackInfo: PlaybackInfo,
    onSongClick: (Song, List<Song>) -> Unit,
    onOpenLibraryTab: (DhunNavTab) -> Unit,
    onOpenSettings: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    val continueListeningList = remember(recentlyPlayed, allSongs) {
        if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs.take(8)
    }

    val recentlyAddedList = remember(allSongs) {
        allSongs.sortedByDescending { it.dateAdded }.take(8)
    }

    val listState = rememberLazyListState()
    val pullDistance = remember { mutableFloatStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val refreshThreshold = with(density) { 88.dp.toPx() }
    val currentOnRefresh by rememberUpdatedState(onRefresh)

    LaunchedEffect(isRefreshing) {
        pullDistance.floatValue = if (isRefreshing) refreshThreshold else 0f
    }

    val pullConnection = remember(listState, isRefreshing, refreshThreshold) {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                if (available.y > 0f && atTop && !isRefreshing) {
                    pullDistance.floatValue = (pullDistance.floatValue + available.y).coerceAtMost(refreshThreshold * 1.5f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0f && pullDistance.floatValue > 0f) {
                    val consumedY = available.y.coerceAtLeast(-pullDistance.floatValue)
                    pullDistance.floatValue += consumedY
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pullDistance.floatValue >= refreshThreshold && !isRefreshing) {
                    pullDistance.floatValue = refreshThreshold
                    currentOnRefresh()
                } else if (!isRefreshing) {
                    pullDistance.floatValue = 0f
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().nestedScroll(pullConnection)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
        // --- Dhun Branded Header & Greeting ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DhunBrandHeader(showTagline = false)

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DhunTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DhunTextPrimary
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DhunTextSecondary
                )
            }
        }

        // --- Quick Access 2x2 Layout ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAccessCard(
                        title = stringResource(R.string.quick_liked_songs),
                        subtitle = "${favoriteSongs.size} tracks",
                        icon = Icons.Filled.Favorite,
                        iconGradient = listOf(DhunPink, DhunPinkDeep),
                        onClick = { onOpenLibraryTab(DhunNavTab.LIBRARY) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_access_liked"
                    )
                    QuickAccessCard(
                        title = stringResource(R.string.quick_recently_played),
                        subtitle = "${recentlyPlayed.size} tracks",
                        icon = Icons.Filled.History,
                        iconGradient = listOf(DhunViolet, DhunVioletDark),
                        onClick = { onOpenLibraryTab(DhunNavTab.LIBRARY) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_access_recent"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAccessCard(
                        title = stringResource(R.string.quick_all_songs),
                        subtitle = "${allSongs.size} tracks",
                        icon = Icons.Filled.MusicNote,
                        iconGradient = listOf(DhunAmber, Color(0xFFD97706)),
                        onClick = { onOpenLibraryTab(DhunNavTab.LIBRARY) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_access_all"
                    )
                    QuickAccessCard(
                        title = stringResource(R.string.quick_playlists),
                        subtitle = "${playlists.size} playlists",
                        icon = Icons.Filled.QueueMusic,
                        iconGradient = listOf(DhunSuccess, Color(0xFF059669)),
                        onClick = { onOpenLibraryTab(DhunNavTab.LIBRARY) },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_access_playlists"
                    )
                }
            }
        }

        // --- Mood / Shortcuts ---
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.section_mood_shortcuts),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        AssistChip(
                            onClick = {
                                if (allSongs.isNotEmpty()) {
                                    val lateNightTunes = allSongs.shuffled()
                                    onSongClick(lateNightTunes.first(), lateNightTunes)
                                }
                            },
                            label = { Text(stringResource(R.string.mood_late_night)) },
                            leadingIcon = { Icon(Icons.Outlined.Nightlight, contentDescription = null, tint = DhunViolet) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DhunCard)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = {
                                if (allSongs.isNotEmpty()) {
                                    val energyTunes = allSongs.shuffled()
                                    onSongClick(energyTunes.first(), energyTunes)
                                }
                            },
                            label = { Text(stringResource(R.string.mood_energy)) },
                            leadingIcon = { Icon(Icons.Outlined.Bolt, contentDescription = null, tint = DhunAmber) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DhunCard)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = {
                                if (allSongs.isNotEmpty()) {
                                    val chillTunes = allSongs.shuffled()
                                    onSongClick(chillTunes.first(), chillTunes)
                                }
                            },
                            label = { Text(stringResource(R.string.mood_chill)) },
                            leadingIcon = { Icon(Icons.Outlined.Spa, contentDescription = null, tint = DhunSuccess) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DhunCard)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = {
                                if (favoriteSongs.isNotEmpty()) {
                                    onSongClick(favoriteSongs.first(), favoriteSongs)
                                }
                            },
                            label = { Text(stringResource(R.string.mood_favorites)) },
                            leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null, tint = DhunPink) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = DhunCard)
                        )
                    }
                }
            }
        }

        if (allSongs.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                DhunEmptyState(
                    icon = Icons.Filled.MusicNote,
                    title = stringResource(R.string.empty_library_title),
                    description = stringResource(R.string.empty_library_desc),
                    actionLabel = stringResource(R.string.scan_music_button),
                    onActionClick = { onOpenLibraryTab(DhunNavTab.LIBRARY) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
                )
            }
        } else {
            // --- Continue Listening (Horizontal Cards) ---
            if (continueListeningList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    SectionHeader(
                        title = stringResource(R.string.section_continue_listening),
                        onSeeAll = { onOpenLibraryTab(DhunNavTab.LIBRARY) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(continueListeningList) { song ->
                            HorizontalSongCard(
                                song = song,
                                isCurrent = song.id == playbackInfo.currentSong?.id,
                                onClick = { onSongClick(song, continueListeningList) }
                            )
                        }
                    }
                }
            }

            // --- Recently Added (Horizontal Cards) ---
            if (recentlyAddedList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    SectionHeader(
                        title = stringResource(R.string.section_recently_added),
                        onSeeAll = { onOpenLibraryTab(DhunNavTab.LIBRARY) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentlyAddedList) { song ->
                            HorizontalSongCard(
                                song = song,
                                isCurrent = song.id == playbackInfo.currentSong?.id,
                                onClick = { onSongClick(song, recentlyAddedList) }
                            )
                        }
                    }
                }
            }
        }
        }
            if (pullDistance.floatValue > 0f || isRefreshing) {
                val progress = (pullDistance.floatValue / refreshThreshold).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = with(density) { (pullDistance.floatValue - 48.dp.toPx()).coerceAtLeast(0f).toDp() })
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(32.dp)) {
                        val stroke = 2.6.dp.toPx()
                        drawCircle(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), style = Stroke(width = stroke))
                        drawArc(
                            color = MaterialTheme.colorScheme.primary,
                            startAngle = -90f,
                            sweepAngle = 285f * progress,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = if (isRefreshing) "Refreshing music library" else "Pull to refresh music library",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
    
@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.see_all),
            style = MaterialTheme.typography.labelLarge,
            color = DhunViolet,
            modifier = Modifier.clickable(onClick = onSeeAll)
        )
    }
}

@Composable
private fun HorizontalSongCard(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(152.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (isCurrent) DhunPink.copy(alpha = 0.6f) else DhunBorder.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("song_card_${song.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) DhunViolet.copy(alpha = 0.16f) else DhunCardElevated
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                ArtworkView(
                    artworkUri = song.albumArtUri,
                    songTitle = song.title,
                    size = 132.dp,
                    shape = RoundedCornerShape(10.dp),
                    iconSize = 44.dp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrent) DhunPink else DhunTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = DhunTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
