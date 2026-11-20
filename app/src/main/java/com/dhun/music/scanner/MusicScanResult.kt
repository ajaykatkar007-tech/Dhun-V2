package com.dhun.music.scanner

import com.dhun.music.model.Song

sealed interface MusicScanResult {
    data class Success(val songs: List<Song>) : MusicScanResult
    data class Empty(val reason: String = "No supported audio files found") : MusicScanResult
    data class PartialSuccess(val songs: List<Song>, val skippedCount: Int) : MusicScanResult
    data object PermissionDenied : MusicScanResult
    data class Failed(val reason: String, val cause: Throwable? = null) : MusicScanResult
    data object Cancelled : MusicScanResult
}

data class MusicScanReport(
    val result: MusicScanResult,
    val newlyFoundCount: Int = 0
)
