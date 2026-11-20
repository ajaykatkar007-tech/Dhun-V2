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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dhun.music.R
import com.dhun.music.ui.components.DhunBrandHeader
import com.dhun.music.ui.theme.DhunAmber
import com.dhun.music.ui.theme.DhunBorder
import com.dhun.music.ui.theme.DhunCard
import com.dhun.music.ui.theme.DhunCardElevated
import com.dhun.music.ui.theme.DhunPink
import com.dhun.music.ui.theme.DhunSuccess
import com.dhun.music.ui.theme.DhunTextPrimary
import com.dhun.music.ui.theme.DhunTextSecondary
import com.dhun.music.ui.theme.DhunViolet
import com.dhun.music.ui.theme.DhunVioletDark

@Composable
fun SettingsScreen(
    isScanning: Boolean,
    onRescanLibrary: (Boolean) -> Unit,
    totalSongCount: Int,
    scanMessage: String? = null,
    onOpenAppSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("dhun_settings", Context.MODE_PRIVATE) }
    var filterShortAudio by remember { mutableStateOf(preferences.getBoolean("ignore_short_audio", true)) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
    ) {
        // Settings Header
        item {
            DhunBrandHeader(modifier = Modifier.padding(bottom = 12.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- LIBRARY SECTION ---
        item {
            SettingsSectionHeader(stringResource(R.string.settings_section_library))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DhunCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Rescan Library
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LibraryMusic, contentDescription = null, tint = DhunAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_rescan_library),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$totalSongCount tracks in local library",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DhunTextSecondary
                                )
                            }
                        }

                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DhunViolet,
                                strokeWidth = 2.dp
                            )
                        } else {
                            OutlinedButton(
                                onClick = { onRescanLibrary(filterShortAudio) },
                                modifier = Modifier.testTag("rescan_library_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter short audio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_filter_short_audio),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ignore ringtones and audio clips under 30s",
                                style = MaterialTheme.typography.bodySmall,
                                color = DhunTextSecondary
                            )
                        }
                        Switch(
                            checked = filterShortAudio,
                            onCheckedChange = {
                                filterShortAudio = it
                                preferences.edit().putBoolean("ignore_short_audio", it).apply()
                                onRescanLibrary(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = DhunViolet
                            )
                        )
                    }
                    if (scanMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = scanMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = DhunTextSecondary
                        )
                        if (scanMessage.startsWith("Storage permission required")) {
                            OutlinedButton(onClick = onOpenAppSettings) {
                                Text("Open app settings")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- APPEARANCE SECTION ---
        item {
            SettingsSectionHeader(stringResource(R.string.settings_section_appearance))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DhunCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = DhunViolet)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Theme & Palette",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Indian-Modern Deep Midnight",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DhunTextSecondary
                                )
                            }
                        }

                        // Gradient circle badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(DhunViolet, DhunPink)))
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- ABOUT SECTION ---
        item {
            SettingsSectionHeader(stringResource(R.string.settings_section_about))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DhunCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = DhunSuccess)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dhun Music Player",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.settings_version_info),
                                style = MaterialTheme.typography.bodySmall,
                                color = DhunTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = DhunViolet)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "100% Offline & Private. No accounts, no data tracking, no ads.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DhunTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = DhunViolet,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}
