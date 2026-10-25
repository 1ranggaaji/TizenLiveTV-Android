package com.tizen.livetv.data

import com.tizen.livetv.model.Channel
import com.tizen.livetv.model.ChannelGenre
import com.tizen.livetv.model.StreamType
import java.io.BufferedReader
import java.io.StringReader

object M3uParser {

    /**
     * Parses M3U or M3U8 IPTV playlist content into List<Channel>.
     */
    fun parseM3u(content: String, startIndex: Int = 100): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(content))
        var line: String?

        var currentName = ""
        var currentLogo = ""
        var currentGroup = ""
        var currentTvgId = ""
        var currentNumber = startIndex

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTINF:")) {
                // Parse #EXTINF attributes
                currentLogo = extractAttribute(trimmed, "tvg-logo")
                currentGroup = extractAttribute(trimmed, "group-title")
                currentTvgId = extractAttribute(trimmed, "tvg-id")

                // Channel name is after the last comma
                val commaIndex = trimmed.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex + 1 < trimmed.length) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    "Channel $currentNumber"
                }
            } else if (!trimmed.startsWith("#") && (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("rtmp://"))) {
                // This is a stream URL line
                val streamUrl = trimmed
                val streamType = when {
                    streamUrl.contains(".m3u8", ignoreCase = true) -> StreamType.HLS
                    streamUrl.contains(".mpd", ignoreCase = true) -> StreamType.DASH
                    streamUrl.contains(".mp4", ignoreCase = true) -> StreamType.MP4
                    else -> StreamType.HLS
                }

                val genre = mapGroupToGenre(currentGroup)

                val channel = Channel(
                    id = if (currentTvgId.isNotEmpty()) currentTvgId else "custom_ch_$currentNumber",
                    displayNumber = currentNumber.toString(),
                    name = currentName.ifEmpty { "Saluran $currentNumber" },
                    logoUrl = currentLogo.ifEmpty { "https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=200&h=200&fit=crop" },
                    streamUrl = streamUrl,
                    streamType = streamType,
                    genre = genre,
                    isFavorite = false,
                    resolution = "1080p FHD",
                    hdrSupport = "SDR",
                    audioFormat = "Stereo 2.0"
                )
                channels.add(channel)

                currentNumber++
                currentName = ""
                currentLogo = ""
                currentGroup = ""
                currentTvgId = ""
            }
        }

        return channels
    }

    private fun extractAttribute(line: String, attributeName: String): String {
        val pattern = """$attributeName="([^"]*)"""".toRegex()
        val match = pattern.find(line)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun mapGroupToGenre(group: String): ChannelGenre {
        val lower = group.lowercase()
        return when {
            lower.contains("news") || lower.contains("berita") -> ChannelGenre.NEWS
            lower.contains("sport") || lower.contains("olahraga") || lower.contains("bola") -> ChannelGenre.SPORTS
            lower.contains("movie") || lower.contains("film") || lower.contains("cinema") || lower.contains("bioskop") -> ChannelGenre.MOVIES
            lower.contains("doc") || lower.contains("alam") || lower.contains("discovery") -> ChannelGenre.DOCUMENTARY
            lower.contains("music") || lower.contains("musik") -> ChannelGenre.MUSIC
            lower.contains("kid") || lower.contains("kartun") || lower.contains("anak") -> ChannelGenre.KIDS
            else -> ChannelGenre.CUSTOM
        }
    }
}
