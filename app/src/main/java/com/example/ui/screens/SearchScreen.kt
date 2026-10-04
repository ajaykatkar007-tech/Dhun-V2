package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PlaybackInfo
import com.example.model.Song
import com.example.ui.components.DhunEmptyState
import com.example.ui.components.SongRowItem
import com.example.ui.theme.DhunAmber
import com.example.ui.theme.DhunBorder
import com.example.ui.theme.DhunCard
import com.example.ui.theme.DhunPink
import com.example.ui.theme.DhunSuccess
import com.example.ui.theme.DhunTextSecondary
import com.example.ui.theme.DhunViolet

@Composable
fun SearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<Song>,
    allSongs: List<Song>,
    playbackInfo: PlaybackInfo,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowDetails: (Song) -> Unit,
    onShare: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSuggestions = remember {
        listOf("Classical", "Monsoon", "Raga", "Bollywood", "Lofi", "Sitar")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Header
        Text(
            text = stringResource(R.string.search_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
        )

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("search_input_field"),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = DhunTextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = DhunViolet
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = DhunTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DhunCard,
                unfocusedContainerColor = DhunCard,
                focusedBorderColor = DhunViolet,
                unfocusedBorderColor = DhunBorder,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Suggestion Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickSuggestions) { suggestion ->
                SuggestionChip(
                    onClick = { onQueryChange(suggestion) },
                    label = { Text(suggestion) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = DhunCard,
                        labelColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = DhunBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area
        if (searchQuery.isBlank()) {
            // Browse Categories / Empty Query State
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.search_categories),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            title = "Artists",
                            subtitle = "${allSongs.map { it.artist }.distinct().size} found",
                            icon = Icons.Outlined.Person,
                            accentColor = DhunViolet,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val topArtist = allSongs.firstOrNull()?.artist
                                if (topArtist != null) onQueryChange(topArtist)
                            }
                        )
                        CategoryCard(
                            title = "Albums",
                            subtitle = "${allSongs.map { it.album }.distinct().size} albums",
                            icon = Icons.Outlined.Album,
                            accentColor = DhunPink,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val topAlbum = allSongs.firstOrNull()?.album
                                if (topAlbum != null) onQueryChange(topAlbum)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            title = "Folders",
                            subtitle = "${allSongs.map { it.folder }.distinct().size} folders",
                            icon = Icons.Outlined.Folder,
                            accentColor = DhunAmber,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val topFolder = allSongs.firstOrNull()?.folder
                                if (topFolder != null) onQueryChange(topFolder)
                            }
                        )
                        CategoryCard(
                            title = "All Songs",
                            subtitle = "${allSongs.size} tracks",
                            icon = Icons.Default.Search,
                            accentColor = DhunSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = { onQueryChange(" ") }
                        )
                    }
                }
            }
        } else if (searchResults.isEmpty()) {
            // No Results
            DhunEmptyState(
                icon = Icons.Outlined.SearchOff,
                title = stringResource(R.string.search_no_results_title),
                description = stringResource(R.string.search_no_results_desc),
                actionLabel = "Clear Search",
                onActionClick = { onQueryChange("") }
            )
        } else {
            // Display Results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = "Found ${searchResults.size} matches",
                        style = MaterialTheme.typography.bodySmall,
                        color = DhunTextSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                items(searchResults) { song ->
                    SongRowItem(
                        song = song,
                        isPlaying = playbackInfo.isPlaying && song.id == playbackInfo.currentSong?.id,
                        isCurrentTrack = song.id == playbackInfo.currentSong?.id,
                        onClick = { onSongClick(song, searchResults) },
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
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DhunCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DhunTextSecondary
            )
        }
    }
}
