package com.dhun.music.scanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaScannerRulesTest {
    @Test
    fun mp3IsDetectedByMimeTypeOrExtension() {
        assertTrue(isSupportedAudioFile("audio/mpeg", "track.bin"))
        assertTrue(isSupportedAudioFile("application/octet-stream", "track.mp3"))
        assertTrue(isSupportedAudioFile("audio/mpeg", null))
        assertTrue(isSupportedAudioFile(null, "track.MP3"))
    }

    @Test
    fun supportedNonMp3AudioFormatsRemainSupported() {
        assertTrue(isSupportedAudioFile("audio/flac", "track.flac"))
        assertTrue(isSupportedAudioFile(null, "track.wav"))
    }

    @Test
    fun duplicateScanResultsAreDeduplicatedByMediaIdAndUri() {
        val first = com.dhun.music.model.Song(1L, "A", "Artist", "Album", 60000L, "content://audio/1")
        val duplicateId = first.copy(title = "Duplicate metadata")
        val duplicateUri = first.copy(id = 2L)
        val distinct = first.copy(id = 3L, contentUri = "content://audio/3")
        assertTrue(deduplicateScannedSongs(listOf(first, duplicateId, duplicateUri, distinct)).map { it.id } == listOf(1L, 3L))
    }

    @Test
    fun onlyCompleteNonEmptyScanMayReplaceLibrary() {
        val song = com.dhun.music.model.Song(1L, "A", "Artist", "Album", 60000L, "content://audio/1")
        assertTrue(shouldReplaceLibrary(MusicScanResult.Success(listOf(song))))
        assertFalse(shouldReplaceLibrary(MusicScanResult.Success(emptyList())))
        assertFalse(shouldReplaceLibrary(MusicScanResult.Empty()))
        assertFalse(shouldReplaceLibrary(MusicScanResult.PartialSuccess(listOf(song), 1)))
        assertFalse(shouldReplaceLibrary(MusicScanResult.PermissionDenied))
        assertFalse(shouldReplaceLibrary(MusicScanResult.Failed("query failed")))
        assertFalse(shouldReplaceLibrary(MusicScanResult.Cancelled))
    }

    @Test
    fun durationFilterUsesConfiguredThreshold() {
        assertTrue(passesDurationFilter(30_000L, 30_000L))
        assertFalse(passesDurationFilter(29_999L, 30_000L))
        assertTrue(passesDurationFilter(5_000L, 5_000L))
        assertFalse(passesDurationFilter(4_999L, 5_000L))
        assertFalse(passesDurationFilter(0L, 5_000L))
    }
}
