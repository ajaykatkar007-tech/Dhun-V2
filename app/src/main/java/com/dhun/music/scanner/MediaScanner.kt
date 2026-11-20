package com.dhun.music.scanner

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.dhun.music.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

object MediaScanner {

    private val ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart")
    private val SUPPORTED_AUDIO_EXTENSIONS = setOf(
        "mp3", "m4a", "aac", "wav", "ogg", "oga", "flac", "opus", "3gp", "amr"
    )

    suspend fun scanDeviceAudio(
        context: Context,
        minDurationMs: Long = 30_000L
    ): MusicScanResult = withContext(Dispatchers.IO) {
        if (!hasAudioPermission(context)) return@withContext MusicScanResult.PermissionDenied

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
            MediaStore.Audio.Media.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DISPLAY_NAME
        )

        val songs = mutableListOf<Song>()
        var skippedRows = 0

        try {
            val cursor = context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            ) ?: return@withContext MusicScanResult.Failed("MediaStore returned no cursor")

            cursor.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val mimeColumn = it.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val nameColumn = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)

                while (it.moveToNext()) {
                    try {
                        val id = it.getLong(idColumn)
                        val duration = it.getLong(durationColumn)
                        if (id <= 0L || duration < minDurationMs || duration <= 0L) continue

                        val contentUri = ContentUris.withAppendedId(collection, id)
                        val filePath = dataColumn.takeIf { column -> column >= 0 }
                            ?.let(it::getString)
                        val displayName = nameColumn.takeIf { column -> column >= 0 }
                            ?.let(it::getString) ?: filePath?.let { path -> File(path).name }
                        val mimeFromCursor = mimeColumn.takeIf { column -> column >= 0 }
                            ?.let(it::getString)
                        val mimeType = mimeFromCursor
                            ?: context.contentResolver.getType(contentUri)
                        val extension = displayName
                            ?.substringAfterLast('.', "")
                            ?.lowercase(Locale.ROOT)
                        val isMp3 = mimeType.equals("audio/mpeg", ignoreCase = true) ||
                            extension == "mp3"
                        val isOtherSupportedAudio = mimeType?.startsWith("audio/", ignoreCase = true) == true ||
                            extension?.let { it in SUPPORTED_AUDIO_EXTENSIONS } == true

                        if (!isMp3 && !isOtherSupportedAudio) continue

                        // Verify that the MediaStore URI can actually be opened before caching it.
                        val accessible = context.contentResolver.openAssetFileDescriptor(contentUri, "r")
                            ?.use { true } ?: false
                        if (!accessible) {
                            skippedRows++
                            continue
                        }

                        val albumId = it.getLong(albumIdColumn)
                        val title = it.getString(titleColumn)?.takeIf(String::isNotBlank) ?: "Track $id"
                        val artist = it.getString(artistColumn)?.takeIf(String::isNotBlank)
                            ?.takeUnless { value -> value == "<unknown>" } ?: "Unknown Artist"
                        val album = it.getString(albumColumn)?.takeIf(String::isNotBlank)
                            ?.takeUnless { value -> value == "<unknown>" } ?: "Unknown Album"
                        val dateAdded = it.getLong(dateAddedColumn)
                        val folder = filePath?.let { path -> File(path).parentFile?.name } ?: "Music"
                        val albumArtUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId).toString()

                        songs.add(
                            Song(
                                id = id,
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                contentUri = contentUri.toString(),
                                albumArtUri = albumArtUri,
                                folder = folder,
                                dateAdded = dateAdded,
                                isLocalSample = false
                            )
                        )
                    } catch (security: SecurityException) {
                        return@withContext MusicScanResult.PermissionDenied
                    } catch (rowError: Exception) {
                        skippedRows++
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            return@withContext MusicScanResult.Cancelled
        } catch (security: SecurityException) {
            return@withContext MusicScanResult.PermissionDenied
        } catch (error: Exception) {
            return@withContext MusicScanResult.Failed(
                error.message ?: "Unable to scan device audio",
                error
            )
        }

        val uniqueSongs = songs.distinctBy { it.id }.distinctBy { it.contentUri }
        when {
            uniqueSongs.isEmpty() && skippedRows > 0 ->
                MusicScanResult.Failed("Audio scan was incomplete; $skippedRows item(s) could not be read")
            uniqueSongs.isEmpty() -> MusicScanResult.Empty()
            skippedRows > 0 -> MusicScanResult.PartialSuccess(uniqueSongs, skippedRows)
            else -> MusicScanResult.Success(uniqueSongs)
        }
    }


    suspend fun countDeviceAudioItems(context: Context): Int? = withContext(Dispatchers.IO) {
        if (!hasAudioPermission(context)) return@withContext null
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        try {
            val cursor = context.contentResolver.query(
                collection,
                arrayOf(MediaStore.Audio.Media._ID),
                null,
                null,
                null
            ) ?: return@withContext null
            cursor.use { it.count }
        } catch (_: Exception) {
            null
        }
    }

    private fun hasAudioPermission(context: Context): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
