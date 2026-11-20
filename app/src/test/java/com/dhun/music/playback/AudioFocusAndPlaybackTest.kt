package com.dhun.music.playback

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import com.dhun.music.model.PlayerStatus
import com.dhun.music.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class AudioFocusAndPlaybackTest {

    private lateinit var context: Context
    private lateinit var playbackManager: AudioPlaybackManager

    private val testSongA = Song(
        id = 101L,
        title = "Morning Raga",
        artist = "Pt. Hariprasad",
        album = "Classical Dawn",
        durationMs = 180_000L,
        contentUri = "file:///dummy/song_a.mp3",
        isFavorite = false
    )

    private val testSongB = Song(
        id = 102L,
        title = "Monsoon Beat",
        artist = "Dhun Collective",
        album = "Indian Rain Beats",
        durationMs = 210_000L,
        contentUri = "file:///dummy/song_b.mp3",
        isFavorite = false
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        playbackManager = AudioPlaybackManager.getInstance(context)
        playbackManager.clearQueue()
    }

    @Test
    fun testManualUserPause_neverResumesOnAudioFocusGain() {
        // Step 1: User explicitly starts playing a song
        playbackManager.playSong(testSongA, listOf(testSongA, testSongB))
        assertFalse("User should not be in manual pause state on start", playbackManager.isManuallyPausedByUser)

        // Step 2: User explicitly taps Pause
        playbackManager.pause()
        assertTrue("Manual user pause flag MUST be true", playbackManager.isManuallyPausedByUser)
        assertFalse("Should not be marked as paused due to transient focus loss", playbackManager.pausedDueToTransientFocusLoss)
        assertEquals(PlayerStatus.PAUSED, playbackManager.playbackInfo.value.status)

        // Step 3: External app (e.g. YouTube) runs, and then system sends AUDIOFOCUS_GAIN back to Dhun
        playbackManager.handleAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)

        // CRITICAL INVARIANT: Manual user pause MUST ALWAYS override automatic resume
        assertTrue("Manual user pause flag MUST remain true", playbackManager.isManuallyPausedByUser)
        assertEquals("Player MUST remain PAUSED and never automatically resume after manual user pause",
            PlayerStatus.PAUSED, playbackManager.playbackInfo.value.status)
        assertFalse("isPlaying MUST be false", playbackManager.playbackInfo.value.isPlaying)
    }

    @Test
    fun testTransientFocusLoss_resumesOnAudioFocusGainOnlyIfUserDidNotPause() {
        // Step 1: Player is playing
        playbackManager.playSong(testSongA, listOf(testSongA, testSongB))

        // Step 2: Transient audio focus loss occurs (e.g. phone call or nav prompt)
        playbackManager.handleAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
        assertFalse("Should NOT be marked as manually paused by user", playbackManager.isManuallyPausedByUser)
        assertTrue("Should be marked as paused due to transient focus loss", playbackManager.pausedDueToTransientFocusLoss)

        // Step 3: Audio focus is regained
        playbackManager.handleAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        assertFalse("Transient focus flag should be cleared", playbackManager.pausedDueToTransientFocusLoss)
        assertFalse("User pause flag should still be false", playbackManager.isManuallyPausedByUser)
    }

    @Test
    fun testFavoriteToggle_doesNotTriggerTrackChangeOrNavigation() {
        // Step 1: Start playing track A in queue [A, B]
        val queue = listOf(testSongA, testSongB)
        playbackManager.playSong(testSongA, queue)

        assertEquals(testSongA.id, playbackManager.playbackInfo.value.currentSong?.id)
        assertEquals(0, playbackManager.playbackInfo.value.queueIndex)
        assertEquals(2, playbackManager.playbackInfo.value.queue.size)

        // Step 2: User favorites track A
        val statusBefore = playbackManager.playbackInfo.value.status
        val isPlayingBefore = playbackManager.playbackInfo.value.isPlaying
        val shuffleBefore = playbackManager.playbackInfo.value.isShuffle
        val repeatBefore = playbackManager.playbackInfo.value.repeatMode
        val posBefore = playbackManager.playbackInfo.value.currentPositionMs

        playbackManager.updateSongFavorite(testSongA.id, true)

        // Invariants: Track position, current song ID, queue order, status, shuffle, repeat must NOT change
        assertEquals("Current song ID must remain testSongA", testSongA.id, playbackManager.playbackInfo.value.currentSong?.id)
        assertTrue("Current song must now have isFavorite = true", playbackManager.playbackInfo.value.currentSong?.isFavorite == true)
        assertEquals("Queue index must still be 0", 0, playbackManager.playbackInfo.value.queueIndex)
        assertEquals("Queue length must remain 2", 2, playbackManager.playbackInfo.value.queue.size)
        assertTrue("Queue element 0 must have isFavorite = true", playbackManager.playbackInfo.value.queue[0].isFavorite)
        assertEquals("Status must not change on favorite toggle", statusBefore, playbackManager.playbackInfo.value.status)
        assertEquals("isPlaying must not change on favorite toggle", isPlayingBefore, playbackManager.playbackInfo.value.isPlaying)
        assertEquals("Shuffle must not change on favorite toggle", shuffleBefore, playbackManager.playbackInfo.value.isShuffle)
        assertEquals("Repeat must not change on favorite toggle", repeatBefore, playbackManager.playbackInfo.value.repeatMode)
        assertEquals("Position must not change on favorite toggle", posBefore, playbackManager.playbackInfo.value.currentPositionMs)

        // Step 3: User unfavorites track A
        playbackManager.updateSongFavorite(testSongA.id, false)
        assertEquals(testSongA.id, playbackManager.playbackInfo.value.currentSong?.id)
        assertFalse("Current song must now have isFavorite = false", playbackManager.playbackInfo.value.currentSong?.isFavorite == true)
        assertEquals(0, playbackManager.playbackInfo.value.queueIndex)
    }
}
