package com.example.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object MediaScanner {

    private val ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanDeviceAudio(
        context: Context,
        minDurationMs: Long = 30_000L
    ): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val contentResolver = context.contentResolver

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(minDurationMs.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Track $id"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val albumArtUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId).toString()

                    val filePath = if (dataColumn != -1) cursor.getString(dataColumn) else null
                    val folder = filePath?.let { File(it).parentFile?.name } ?: "Music"

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = duration,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            folder = folder,
                            dateAdded = dateAdded,
                            isLocalSample = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If device has no local audio or permission hasn't provided tracks yet,
        // provide Indian-modern ambient Dhun sample tracks
        if (songs.isEmpty()) {
            val sampleTracks = getOrGenerateSampleTracks(context)
            songs.addAll(sampleTracks)
        }

        songs
    }

    suspend fun getOrGenerateSampleTracks(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val samplesDir = File(context.filesDir, "dhun_tunes")
        if (!samplesDir.exists()) {
            samplesDir.mkdirs()
        }

        val sampleMetadata = listOf(
            SampleMeta(1001L, "Raag Bhairav - Morning Serenade", "Pt. Hariprasad & Dhun", "Classical Dawn", 180_000L, 220.0, "Morning Ragas"),
            SampleMeta(1002L, "Monsoon in Mumbai", "Dhun Acoustic Collective", "Indian Rain Beats", 210_000L, 261.63, "Monsoon Melodies"),
            SampleMeta(1003L, "Sitar & Sunset Chill", "Ravi Shankar Tribute", "Varanasi Chill", 195_000L, 293.66, "Evening Dhun"),
            SampleMeta(1004L, "Bhangra Groove & Dhol", "Amritsar Pulse", "Punjab Rhythms", 165_000L, 329.63, "Celebration"),
            SampleMeta(1005L, "Sufi Nights & Darbari", "Nusrat Echoes", "Mystic Sufi", 240_000L, 349.23, "Devotional"),
            SampleMeta(1006L, "Lofi Chai & Guitar", "Dhun Lofi Studio", "Late Night Chai", 150_000L, 392.00, "Chill Lounge")
        )

        val sampleSongs = mutableListOf<Song>()

        for (meta in sampleMetadata) {
            val wavFile = File(samplesDir, "dhun_sample_${meta.id}.wav")
            if (!wavFile.exists() || wavFile.length() == 0L) {
                try {
                    writeSampleWavFile(wavFile, meta.baseFreq, durationSeconds = 12)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            sampleSongs.add(
                Song(
                    id = meta.id,
                    title = meta.title,
                    artist = meta.artist,
                    album = meta.album,
                    durationMs = meta.durationMs,
                    contentUri = Uri.fromFile(wavFile).toString(),
                    albumArtUri = null,
                    folder = meta.folder,
                    dateAdded = System.currentTimeMillis() - (meta.id * 100000),
                    isLocalSample = true
                )
            )
        }

        sampleSongs
    }

    private fun writeSampleWavFile(file: File, baseFreq: Double, durationSeconds: Int) {
        val sampleRate = 44100
        val numSamples = durationSeconds * sampleRate
        val audioData = ShortArray(numSamples)

        // Generate warm Indian harmonium / flute ambient tone with harmonics
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Main melodic wave with overtone warmth
            val wave = sin(2 * PI * baseFreq * t) * 0.5 +
                    sin(2 * PI * baseFreq * 1.5 * t) * 0.25 +
                    sin(2 * PI * baseFreq * 2.0 * t) * 0.15

            // Smooth envelope to prevent clicks
            val attack = (i.toDouble() / (sampleRate * 0.5)).coerceAtMost(1.0)
            val release = ((numSamples - i).toDouble() / (sampleRate * 0.5)).coerceAtMost(1.0).coerceAtLeast(0.0)
            val envelope = attack * release

            audioData[i] = (wave * envelope * Short.MAX_VALUE * 0.7).toInt().toShort()
        }

        val totalDataLen = numSamples * 2
        val totalAudioLen = totalDataLen + 36

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put("RIFF".toByteArray())
                putInt(totalAudioLen)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // Subchunk1Size
                putShort(1) // PCM format
                putShort(1) // Mono
                putInt(sampleRate)
                putInt(sampleRate * 2) // Byte rate
                putShort(2) // Block align
                putShort(16) // Bits per sample
                put("data".toByteArray())
                putInt(totalDataLen)
            }
            out.write(header.array())

            val byteBuffer = ByteBuffer.allocate(totalDataLen).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                for (sample in audioData) {
                    putShort(sample)
                }
            }
            out.write(byteBuffer.array())
        }
    }

    private data class SampleMeta(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val baseFreq: Double,
        val folder: String
    )
}
