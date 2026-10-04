package com.dhun.music.model

data class Playlist(
    val id: Long = 0L,
    val name: String,
    val songCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun formatDuration(): String {
        val totalSeconds = (totalDurationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val hours = minutes / 60
        return if (hours > 0) {
            String.format("%d hr %d min", hours, minutes % 60)
        } else {
            String.format("%d min", minutes)
        }
    }
}
