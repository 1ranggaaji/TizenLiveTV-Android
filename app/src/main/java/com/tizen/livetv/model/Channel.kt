package com.tizen.livetv.model

enum class StreamType {
    HLS,
    DASH,
    MP4,
    AUTO
}

data class Channel(
    val id: String,
    val displayNumber: String,
    val name: String,
    val logoUrl: String,
    val streamUrl: String,
    val streamType: StreamType = StreamType.AUTO,
    val genre: ChannelGenre = ChannelGenre.ENTERTAINMENT,
    var isFavorite: Boolean = false,
    val resolution: String = "1080p FHD",
    val hdrSupport: String = "HDR10",
    val audioFormat: String = "Dolby Audio 5.1",
    val programs: MutableList<Program> = mutableListOf()
) {
    val currentProgram: Program?
        get() {
            val now = System.currentTimeMillis()
            return programs.firstOrNull { now in it.startTimeMs..it.endTimeMs } 
                ?: programs.firstOrNull()
        }

    val nextProgram: Program?
        get() {
            val now = System.currentTimeMillis()
            val current = currentProgram ?: return null
            val currentIndex = programs.indexOf(current)
            if (currentIndex >= 0 && currentIndex + 1 < programs.size) {
                return programs[currentIndex + 1]
            }
            return programs.firstOrNull { it.startTimeMs >= current.endTimeMs }
        }

    val formattedNumber: String
        get() = displayNumber.padStart(3, '0')
}
