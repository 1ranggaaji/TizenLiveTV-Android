package com.tizen.livetv.model

enum class ChannelGenre(val displayName: String, val iconResName: String) {
    ALL("Semua Saluran", "ic_genre_all"),
    FAVORITES("Favorit", "ic_genre_favorite"),
    NEWS("Berita", "ic_genre_news"),
    SPORTS("Olahraga", "ic_genre_sports"),
    MOVIES("Film & Bioskop", "ic_genre_movies"),
    ENTERTAINMENT("Hiburan", "ic_genre_entertainment"),
    DOCUMENTARY("Dokumenter & Alam", "ic_genre_documentary"),
    MUSIC("Musik", "ic_genre_music"),
    KIDS("Anak & Animasi", "ic_genre_kids"),
    CUSTOM("IPTV Saya", "ic_genre_custom");

    companion object {
        fun fromString(name: String?): ChannelGenre {
            if (name == null) return ALL
            return entries.firstOrNull { 
                it.name.equals(name, ignoreCase = true) || 
                it.displayName.equals(name, ignoreCase = true) 
            } ?: ALL
        }
    }
}
