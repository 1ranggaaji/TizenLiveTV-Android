package com.tizen.livetv.data

import com.tizen.livetv.model.Channel
import com.tizen.livetv.model.ChannelGenre
import com.tizen.livetv.model.Program
import com.tizen.livetv.model.StreamType

object SampleChannels {

    fun getSampleChannels(): List<Channel> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L

        val channelList = listOf(
            Channel(
                id = "ch_001",
                displayNumber = "1",
                name = "Samsung TV Plus HD",
                logoUrl = "https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=200&h=200&fit=crop",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamType = StreamType.HLS,
                genre = ChannelGenre.ENTERTAINMENT,
                isFavorite = true,
                resolution = "4K UHD",
                hdrSupport = "HDR10+",
                audioFormat = "Dolby Atmos 7.1"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Hiburan Spesial", now, hour))
            },

            Channel(
                id = "ch_002",
                displayNumber = "2",
                name = "Bloomberg News Live",
                logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=200&h=200&fit=crop",
                streamUrl = "https://bloomberg-bloombergtv-1-eu.rakuten.wurl.tv/playlist.m3u8",
                streamType = StreamType.HLS,
                genre = ChannelGenre.NEWS,
                isFavorite = true,
                resolution = "1080p FHD",
                hdrSupport = "SDR",
                audioFormat = "Stereo 2.0"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Berita Bisnis & Pasar Global", now, hour))
            },

            Channel(
                id = "ch_003",
                displayNumber = "3",
                name = "NASA TV 4K",
                logoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=200&h=200&fit=crop",
                streamUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                streamType = StreamType.HLS,
                genre = ChannelGenre.DOCUMENTARY,
                isFavorite = true,
                resolution = "4K UHD",
                hdrSupport = "HDR10",
                audioFormat = "Dolby 5.1"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Eksplorasi Luar Angkasa ISS", now, hour))
            },

            Channel(
                id = "ch_004",
                displayNumber = "4",
                name = "Red Bull TV Sports",
                logoUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=200&h=200&fit=crop",
                streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
                streamType = StreamType.HLS,
                genre = ChannelGenre.SPORTS,
                isFavorite = true,
                resolution = "1080p 60fps",
                hdrSupport = "HDR10",
                audioFormat = "Dolby 5.1"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Kejuaraan Olahraga Ekstrem", now, hour))
            },

            // From androidtv-sample-inputs: Tears of Steel (MPEG-DASH)
            Channel(
                id = "ch_005",
                displayNumber = "5",
                name = "Cinema Sci-Fi (Tears of Steel)",
                logoUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=200&h=200&fit=crop",
                streamUrl = "https://dash.akamaized.net/dash264/TestCases/1c/qualcomm/1/Cosmos.mpd",
                streamType = StreamType.DASH,
                genre = ChannelGenre.MOVIES,
                isFavorite = false,
                resolution = "4K Cinema",
                hdrSupport = "Dolby Vision",
                audioFormat = "Dolby Atmos"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Film Sains Fiksi Blockbuster", now, hour))
            },

            // From androidtv-sample-inputs: Big Buck Bunny (HLS)
            Channel(
                id = "ch_006",
                displayNumber = "6",
                name = "Animation Kids (Big Buck Bunny)",
                logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=200&h=200&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                streamType = StreamType.MP4,
                genre = ChannelGenre.KIDS,
                isFavorite = false,
                resolution = "1080p FHD",
                hdrSupport = "SDR",
                audioFormat = "Stereo 2.0"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Petualangan Kartun Lucu", now, hour))
            },

            // From androidtv-sample-inputs: Sintel (MP4 / Google Sample)
            Channel(
                id = "ch_007",
                displayNumber = "7",
                name = "Action Movies (Sintel)",
                logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=200&h=200&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                streamType = StreamType.MP4,
                genre = ChannelGenre.MOVIES,
                isFavorite = false,
                resolution = "1080p FHD",
                hdrSupport = "HDR10",
                audioFormat = "Dolby 5.1"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Kisah Prajurit & Naga", now, hour))
            },

            Channel(
                id = "ch_008",
                displayNumber = "8",
                name = "TVRI Nasional HD",
                logoUrl = "https://images.unsplash.com/photo-1598387993441-a364f854c3e1?w=200&h=200&fit=crop",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamType = StreamType.HLS,
                genre = ChannelGenre.NEWS,
                isFavorite = false,
                resolution = "1080p FHD",
                hdrSupport = "SDR",
                audioFormat = "Stereo 2.0"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Indonesia Terkini & Budaya", now, hour))
            },

            Channel(
                id = "ch_009",
                displayNumber = "9",
                name = "Nature & Wildlife 4K",
                logoUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=200&h=200&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                streamType = StreamType.MP4,
                genre = ChannelGenre.DOCUMENTARY,
                isFavorite = false,
                resolution = "4K UHD",
                hdrSupport = "HDR10+",
                audioFormat = "Dolby 5.1"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Keajaiban Hutan Tropis & Laut", now, hour))
            },

            Channel(
                id = "ch_010",
                displayNumber = "10",
                name = "World Music Hits",
                logoUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&h=200&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                streamType = StreamType.MP4,
                genre = ChannelGenre.MUSIC,
                isFavorite = false,
                resolution = "1080p FHD",
                hdrSupport = "SDR",
                audioFormat = "Dolby Atmos"
            ).apply {
                programs.addAll(generateProgramsForChannel(id, name, "Top Chart Musik Internasional", now, hour))
            }
        )

        return channelList
    }

    private fun generateProgramsForChannel(
        channelId: String,
        channelName: String,
        baseTheme: String,
        now: Long,
        hourMs: Long
    ): List<Program> {
        val programs = mutableListOf<Program>()
        // Generate a 24-hour schedule starting 2 hours before current time
        val startTime = now - (2 * hourMs)

        val scheduleTitles = listOf(
            "Sorotan Pagi: Dunia Hari Ini",
            "Wawasan $baseTheme: Edisi Khusus",
            "Live Show: $channelName Eksklusif",
            "Prime Time Premiere: $baseTheme Spesial",
            "Ulasan Malam & Analisis Terkini",
            "Dokumenter Spesial: Planet Bumi & Teknologi",
            "Tayangan Larut Malam & Rekap Sepekan",
            "Kilasan Pagi & Berita Utama"
        )

        val durations = listOf(90, 60, 120, 90, 60, 120, 60, 60) // in minutes
        var cursorTime = startTime

        scheduleTitles.forEachIndexed { index, title ->
            val durationMs = durations[index % durations.size] * 60 * 1000L
            val endTime = cursorTime + durationMs

            programs.add(
                Program(
                    id = "prog_${channelId}_$index",
                    channelId = channelId,
                    title = title,
                    episodeTitle = "Episode ${index + 1}",
                    description = "Menyajikan liputan terlengkap, tayangan audio visual kualitas tinggi dengan tata suara jernih surround sound langsung di ruang keluarga Anda.",
                    startTimeMs = cursorTime,
                    endTimeMs = endTime,
                    genre = baseTheme,
                    videoQuality = if (index % 2 == 0) "4K UHD" else "1080p FHD",
                    audioQuality = "Dolby 5.1",
                    ageRating = if (index == 2 || index == 3) "PG-13" else "Semua Umur"
                )
            )

            cursorTime = endTime
        }

        return programs
    }
}
