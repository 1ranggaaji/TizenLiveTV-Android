package com.tizen.livetv.data

import android.content.ContentValues
import android.content.Context
import android.media.tv.TvContract
import android.net.Uri
import android.util.Log
import androidx.tvprovider.media.tv.ChannelHeader
import androidx.tvprovider.media.tv.TvContractCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tizen.livetv.model.Channel
import com.tizen.livetv.model.ChannelGenre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChannelRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("tizen_live_tv_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val channels = mutableListOf<Channel>()

    init {
        loadChannels()
    }

    private fun loadChannels() {
        val json = prefs.getString("saved_channels_json", null)
        if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Channel>>() {}.type
                val loaded: List<Channel> = gson.fromJson(json, type)
                if (loaded.isNotEmpty()) {
                    channels.clear()
                    channels.addAll(loaded)
                    return
                }
            } catch (e: Exception) {
                Log.e("ChannelRepository", "Error loading saved channels: ${e.message}")
            }
        }

        // Fallback to sample channels
        channels.clear()
        channels.addAll(SampleChannels.getSampleChannels())
        saveChannels()
    }

    fun getAllChannels(): List<Channel> = channels.toList()

    fun getChannelsByGenre(genre: ChannelGenre): List<Channel> {
        return when (genre) {
            ChannelGenre.ALL -> channels
            ChannelGenre.FAVORITES -> channels.filter { it.isFavorite }
            else -> channels.filter { it.genre == genre }
        }
    }

    fun getChannelById(id: String): Channel? {
        return channels.firstOrNull { it.id == id }
    }

    fun getChannelByNumber(number: String): Channel? {
        return channels.firstOrNull { it.displayNumber == number || it.formattedNumber == number }
    }

    fun getNextChannel(currentChannel: Channel): Channel {
        if (channels.isEmpty()) return currentChannel
        val index = channels.indexOfFirst { it.id == currentChannel.id }
        val nextIndex = if (index >= 0 && index + 1 < channels.size) index + 1 else 0
        return channels[nextIndex]
    }

    fun getPreviousChannel(currentChannel: Channel): Channel {
        if (channels.isEmpty()) return currentChannel
        val index = channels.indexOfFirst { it.id == currentChannel.id }
        val prevIndex = if (index > 0) index - 1 else channels.size - 1
        return channels[prevIndex]
    }

    fun toggleFavorite(channelId: String): Boolean {
        val channel = channels.firstOrNull { it.id == channelId } ?: return false
        channel.isFavorite = !channel.isFavorite
        saveChannels()
        return channel.isFavorite
    }

    fun saveLastWatchedChannelId(id: String) {
        prefs.edit().putString("last_watched_channel_id", id).apply()
    }

    fun getLastWatchedChannel(): Channel {
        val lastId = prefs.getString("last_watched_channel_id", null)
        return (if (lastId != null) getChannelById(lastId) else null) 
            ?: channels.firstOrNull() 
            ?: SampleChannels.getSampleChannels().first()
    }

    fun addCustomM3uChannels(m3uContent: String): Int {
        val parsed = M3uParser.parseM3u(m3uContent, startIndex = channels.size + 1)
        if (parsed.isNotEmpty()) {
            channels.addAll(parsed)
            saveChannels()
        }
        return parsed.size
    }

    fun resetToDefaults() {
        channels.clear()
        channels.addAll(SampleChannels.getSampleChannels())
        saveChannels()
    }

    private fun saveChannels() {
        val json = gson.toJson(channels)
        prefs.edit().putString("saved_channels_json", json).apply()
    }

    /**
     * Synchronizes local channels to Android TV's system TvContract database (TIF framework)
     * so that Android TV's system TV provider recognizes these channels.
     */
    suspend fun syncToSystemTvProvider(inputServiceId: String) = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            for (channel in channels) {
                val values = ContentValues().apply {
                    put(TvContract.Channels.COLUMN_INPUT_ID, inputServiceId)
                    put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, channel.displayNumber)
                    put(TvContract.Channels.COLUMN_DISPLAY_NAME, channel.name)
                    put(TvContract.Channels.COLUMN_TYPE, TvContract.Channels.TYPE_PREVIEW)
                    put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, channel.streamUrl)
                }
                resolver.insert(TvContract.Channels.CONTENT_URI, values)
            }
            Log.d("ChannelRepository", "Synced ${channels.size} channels to system TvProvider")
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Could not sync to TvProvider (permission or device restriction): ${e.message}")
        }
    }
}
