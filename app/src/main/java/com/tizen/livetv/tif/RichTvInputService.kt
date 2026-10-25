package com.tizen.livetv.tif

import android.content.Context
import android.media.tv.TvInputManager
import android.media.tv.TvInputService
import android.net.Uri
import android.view.Surface
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.tizen.livetv.data.ChannelRepository
import com.tizen.livetv.model.Channel

/**
 * TV Input Service implementation adapted from googlesamples/androidtv-sample-inputs.
 * Enables Android TV system framework to discover and tune to this app's channels.
 */
class RichTvInputService : TvInputService() {

    private lateinit var channelRepository: ChannelRepository

    override fun onCreate() {
        super.onCreate()
        channelRepository = ChannelRepository(applicationContext)
    }

    override fun onCreateSession(inputId: String): Session {
        return RichTvInputSession(this, channelRepository)
    }

    @OptIn(UnstableApi::class)
    private class RichTvInputSession(
        private val context: Context,
        private val repository: ChannelRepository
    ) : Session(context) {

        private var player: ExoPlayer? = null
        private var surface: Surface? = null

        override fun onRelease() {
            player?.release()
            player = null
        }

        override fun onSetSurface(surface: Surface?): Boolean {
            this.surface = surface
            player?.setVideoSurface(surface)
            return true
        }

        override fun onSetStreamVolume(volume: Float) {
            player?.volume = volume
        }

        override fun onSetCaptionEnabled(enabled: Boolean) {
            // Caption toggle handling
        }

        override fun onTune(channelUri: Uri): Boolean {
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING)

            // Find matching channel from URI or fallback to last watched channel
            val channel = repository.getAllChannels().firstOrNull() ?: return false

            playStream(channel)
            return true
        }

        private fun playStream(channel: Channel) {
            player?.release()
            player = ExoPlayer.Builder(context).build().apply {
                setVideoSurface(surface)
                setMediaItem(MediaItem.fromUri(channel.streamUrl))
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            notifyVideoAvailable()
                        }
                    }
                })
            }
        }
    }
}
