package com.tizen.livetv.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.tizen.livetv.model.Channel
import com.tizen.livetv.model.StreamType

@OptIn(UnstableApi::class)
class LiveTvPlayerManager(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var currentPlayerView: PlayerView? = null
    private var currentChannel: Channel? = null

    var onPlaybackStateChanged: ((isBuffering: Boolean, isPlaying: Boolean) -> Unit)? = null
    var onError: ((errorMessage: String) -> Unit)? = null
    var onVideoSizeChanged: ((width: Int, height: Int) -> Unit)? = null

    var currentAspectRatioMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        // Low latency buffer configuration optimized for Live TV
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500,  // Min buffer ms
                5000,  // Max buffer ms
                500,   // Playback start buffer ms
                1000   // Rebuffer ms
            )
            .build()

        exoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(playerListener)
            }
    }

    fun attachPlayerView(playerView: PlayerView) {
        currentPlayerView = playerView
        playerView.player = exoPlayer
        playerView.useController = false
        playerView.resizeMode = currentAspectRatioMode
    }

    fun playChannel(channel: Channel) {
        currentChannel = channel
        val player = exoPlayer ?: return

        try {
            val mediaSource = buildMediaSource(channel)
            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            Log.e("LiveTvPlayerManager", "Failed to play channel: ${channel.name}", e)
            onError?.invoke("Gagal memuat siaran: ${e.message}")
        }
    }

    private fun buildMediaSource(channel: Channel): MediaSource {
        val uri = Uri.parse(channel.streamUrl)
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Samsung-Tizen-TV/8.0 (Linux; Android TV)")
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(8000)
            .setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val type = when (channel.streamType) {
            StreamType.HLS -> StreamType.HLS
            StreamType.DASH -> StreamType.DASH
            StreamType.MP4 -> StreamType.MP4
            StreamType.AUTO -> {
                when {
                    channel.streamUrl.contains(".m3u8", ignoreCase = true) -> StreamType.HLS
                    channel.streamUrl.contains(".mpd", ignoreCase = true) -> StreamType.DASH
                    else -> StreamType.MP4
                }
            }
        }

        return when (type) {
            StreamType.HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(MediaItem.fromUri(uri))

            StreamType.DASH -> DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))

            StreamType.MP4, StreamType.AUTO -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        }
    }

    fun cycleAspectRatio(): String {
        currentAspectRatioMode = when (currentAspectRatioMode) {
            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        currentPlayerView?.resizeMode = currentAspectRatioMode

        return when (currentAspectRatioMode) {
            AspectRatioFrameLayout.RESIZE_MODE_FIT -> "Rasio: Asli (16:9 Fit)"
            AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Rasio: Penuh Layar (Fill/Stretch)"
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Rasio: Zoom (Tanpa Garis Hitam)"
            else -> "Rasio: Standar"
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun release() {
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
        currentPlayerView = null
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val isBuffering = playbackState == Player.STATE_BUFFERING
            val isPlaying = playbackState == Player.STATE_READY && (exoPlayer?.isPlaying ?: false)
            onPlaybackStateChanged?.invoke(isBuffering, isPlaying)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val isBuffering = exoPlayer?.playbackState == Player.STATE_BUFFERING
            onPlaybackStateChanged?.invoke(isBuffering, isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("LiveTvPlayerManager", "Player error: ${error.message}", error)
            onError?.invoke("Kesalahan pemutaran: ${error.errorCodeName}")
            // Auto retry on live broadcast interruption
            currentChannel?.let { ch ->
                currentPlayerView?.postDelayed({
                    playChannel(ch)
                }, 3000)
            }
        }

        override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
            onVideoSizeChanged?.invoke(videoSize.width, videoSize.height)
        }
    }
}
