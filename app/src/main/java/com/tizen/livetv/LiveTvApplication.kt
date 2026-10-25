package com.tizen.livetv

import android.app.Application
import android.util.Log
import com.tizen.livetv.data.ChannelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LiveTvApplication : Application() {

    lateinit var channelRepository: ChannelRepository
        private set

    override fun onCreate() {
        super.onCreate()
        channelRepository = ChannelRepository(this)
        Log.d("LiveTvApp", "Tizen Live TV initialized with ${channelRepository.getAllChannels().size} channels.")
    }
}
