package com.dhun.music.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.dhun.music.MainActivity
import com.dhun.music.R
import com.dhun.music.model.PlaybackInfo
import com.dhun.music.model.PlayerStatus
import com.dhun.music.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DhunPlaybackService : Service() {

    private var isNoisyReceiverRegistered = false
    private var mediaSession: MediaSession? = null
    private var playbackObserver: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastNotificationKey: String? = null
    private var lastMetadataSongId: Long? = null

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Preserve the existing safety behavior when headphones disconnect.
                AudioPlaybackManager.getInstance(applicationContext).pause()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerNoisyReceiver()
        createMediaSession()
        observePlayback()
    }

    override fun onDestroy() {
        playbackObserver?.cancel()
        serviceScope.cancel()
        unregisterNoisyReceiver()
        mediaSession?.run {
            isActive = false
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun createMediaSession() {
        mediaSession = MediaSession(this, "DhunPlayback").apply {
            setFlags(
                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    AudioPlaybackManager.getInstance(applicationContext).resume()
                }

                override fun onPause() {
                    AudioPlaybackManager.getInstance(applicationContext).pause()
                }

                override fun onSkipToNext() {
                    AudioPlaybackManager.getInstance(applicationContext).next()
                }

                override fun onSkipToPrevious() {
                    AudioPlaybackManager.getInstance(applicationContext).previous()
                }

                override fun onSeekTo(pos: Long) {
                    AudioPlaybackManager.getInstance(applicationContext).seekTo(pos)
                }

                override fun onStop() {
                    AudioPlaybackManager.getInstance(applicationContext).clearQueue()
                }
            })
            isActive = true
        }
    }

    private fun observePlayback() {
        playbackObserver = serviceScope.launch {
            AudioPlaybackManager.getInstance(applicationContext).playbackInfo.collect { info ->
                updateMediaSession(info)
                val notificationKey = "${info.currentSong?.id}:${info.status}"
                if (info.currentSong != null && notificationKey != lastNotificationKey) {
                    lastNotificationKey = notificationKey
                    updatePlaybackNotification(info)
                } else if (info.currentSong == null) {
                    lastNotificationKey = null
                }
            }
        }
    }

    private fun updateMediaSession(info: PlaybackInfo) {
        val session = mediaSession ?: return
        val song = info.currentSong
        if (song == null) {
            lastMetadataSongId = null
            session.setMetadata(null)
            session.setPlaybackState(
                PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)
                    .setState(PlaybackState.STATE_NONE, 0L, 0f)
                    .build()
            )
            return
        }

        if (lastMetadataSongId != song.id) {
            val metadataBuilder = MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, song.album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, info.durationMs.takeIf { it > 0 } ?: song.durationMs)

            loadArtwork(song.albumArtUri)?.let { artwork ->
                metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artwork)
                metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, artwork)
            }
            session.setMetadata(metadataBuilder.build())
            lastMetadataSongId = song.id
        }

        val actions = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
            PlaybackState.ACTION_SEEK_TO or
            PlaybackState.ACTION_STOP

        val state = when (info.status) {
            PlayerStatus.PLAYING -> PlaybackState.STATE_PLAYING
            PlayerStatus.BUFFERING -> PlaybackState.STATE_BUFFERING
            PlayerStatus.PAUSED -> PlaybackState.STATE_PAUSED
            PlayerStatus.ERROR, PlayerStatus.IDLE -> PlaybackState.STATE_STOPPED
        }
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(state, info.currentPositionMs.coerceAtLeast(0L), if (info.isPlaying) 1f else 0f)
                .build()
        )
    }

    private fun loadArtwork(artworkUri: String?): Bitmap? {
        if (artworkUri.isNullOrBlank()) return null
        return try {
            contentResolver.openInputStream(Uri.parse(artworkUri))?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            registerReceiver(noisyReceiver, filter)
            isNoisyReceiverRegistered = true
        }
    }

    private fun unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(noisyReceiver)
            } catch (_: Exception) {
                // Receiver may already have been unregistered by the system.
            }
            isNoisyReceiverRegistered = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playbackManager = AudioPlaybackManager.getInstance(applicationContext)

        when (intent?.action) {
            ACTION_PLAY -> playbackManager.resume()
            ACTION_PAUSE -> playbackManager.pause()
            ACTION_NEXT -> playbackManager.next()
            ACTION_PREV -> playbackManager.previous()
            ACTION_STOP -> {
                playbackManager.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE -> {
                val info = playbackManager.playbackInfo.value
                val song = info.currentSong
                val title = song?.title ?: intent.getStringExtra(EXTRA_TITLE) ?: "Dhun"
                val artist = song?.artist ?: intent.getStringExtra(EXTRA_ARTIST) ?: "Playing Music"
                val isPlaying = info.currentSong?.let { info.isPlaying }
                    ?: intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                val notification = buildNotification(title, artist, isPlaying)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_STICKY
    }

    private fun updatePlaybackNotification(info: PlaybackInfo) {
        val song = info.currentSong ?: return
        val notification = buildNotification(song.title, song.artist, info.isPlaying)
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(title: String, artist: String, isPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun servicePendingIntent(requestCode: Int, action: String): PendingIntent {
            val actionIntent = Intent(this, DhunPlaybackService::class.java).apply { this.action = action }
            return PendingIntent.getService(
                this, requestCode, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val style = NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Previous", servicePendingIntent(1, ACTION_PREV))
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", servicePendingIntent(2, if (isPlaying) ACTION_PAUSE else ACTION_PLAY))
            .addAction(android.R.drawable.ic_media_next, "Next", servicePendingIntent(3, ACTION_NEXT))
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dhun Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and information for currently playing music"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "dhun_playback_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_PLAY = "com.dhun.music.ACTION_PLAY"
        const val ACTION_PAUSE = "com.dhun.music.ACTION_PAUSE"
        const val ACTION_NEXT = "com.dhun.music.ACTION_NEXT"
        const val ACTION_PREV = "com.dhun.music.ACTION_PREV"
        const val ACTION_STOP = "com.dhun.music.ACTION_STOP"
        const val ACTION_UPDATE = "com.dhun.music.ACTION_UPDATE"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        fun startOrUpdateService(context: Context, song: Song, isPlaying: Boolean) {
            val intent = Intent(context, DhunPlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, song.title)
                putExtra(EXTRA_ARTIST, song.artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DhunPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
