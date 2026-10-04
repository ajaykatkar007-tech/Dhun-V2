package com.dhun.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dhun.music.ui.theme.DhunPink
import com.dhun.music.ui.theme.DhunViolet
import com.dhun.music.ui.theme.DhunVioletDark

@Composable
fun ArtworkView(
    artworkUri: String?,
    songTitle: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        DhunVioletDark,
                        DhunViolet,
                        DhunPink
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!artworkUri.isNullOrEmpty()) {
            AsyncImage(
                model = artworkUri,
                contentDescription = "Artwork for $songTitle",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Stylized musical insignia fallback
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music Note",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
