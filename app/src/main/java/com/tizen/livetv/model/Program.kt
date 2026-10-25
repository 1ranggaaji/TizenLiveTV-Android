package com.tizen.livetv.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Program(
    val id: String,
    val channelId: String,
    val title: String,
    val episodeTitle: String? = null,
    val description: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val posterUrl: String? = null,
    val genre: String = "Entertainment",
    val videoQuality: String = "4K UHD",
    val audioQuality: String = "Dolby 5.1",
    val ageRating: String = "PG-13"
) {
    val durationMinutes: Long
        get() = (endTimeMs - startTimeMs) / (1000 * 60)

    val isLive: Boolean
        get() {
            val now = System.currentTimeMillis()
            return now in startTimeMs..endTimeMs
        }

    val progressPercent: Int
        get() {
            val now = System.currentTimeMillis()
            if (now < startTimeMs) return 0
            if (now > endTimeMs) return 100
            val total = endTimeMs - startTimeMs
            if (total <= 0) return 0
            val elapsed = now - startTimeMs
            return ((elapsed.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
        }

    val timeRangeFormatted: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = sdf.format(Date(startTimeMs))
            val end = sdf.format(Date(endTimeMs))
            return "$start - $end"
        }

    val durationFormatted: String
        get() = "${durationMinutes} Menit"
}
