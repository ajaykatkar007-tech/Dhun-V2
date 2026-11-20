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
    fun durationFilterUsesConfiguredThreshold() {
        assertTrue(passesDurationFilter(30_000L, 30_000L))
        assertFalse(passesDurationFilter(29_999L, 30_000L))
        assertTrue(passesDurationFilter(5_000L, 5_000L))
        assertFalse(passesDurationFilter(4_999L, 5_000L))
        assertFalse(passesDurationFilter(0L, 5_000L))
    }
}
