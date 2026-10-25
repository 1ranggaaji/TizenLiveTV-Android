package com.tizen.livetv.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tizen.livetv.LiveTvApplication
import com.tizen.livetv.R
import com.tizen.livetv.data.ChannelRepository
import com.tizen.livetv.model.Channel
import com.tizen.livetv.model.ChannelGenre
import com.tizen.livetv.model.Program
import com.tizen.livetv.player.LiveTvPlayerManager
import com.tizen.livetv.ui.adapter.ChannelCardAdapter
import com.tizen.livetv.ui.adapter.EpgChannelAdapter
import com.tizen.livetv.ui.adapter.EpgProgramAdapter
import com.tizen.livetv.ui.adapter.GenrePillAdapter
import com.tizen.livetv.ui.dialog.M3uImportDialog
import com.tizen.livetv.ui.dialog.QuickSettingsDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var channelRepository: ChannelRepository
    private lateinit var playerManager: LiveTvPlayerManager

    private var currentChannel: Channel? = null

    // UI Views
    private lateinit var playerView: PlayerView
    private lateinit var layoutBuffering: LinearLayout
    private lateinit var tvBufferingMsg: TextView
    private lateinit var tvQuickFeedback: TextView

    // Overlays
    private lateinit var viewInfoBanner: View
    private lateinit var viewSmartHub: View
    private lateinit var viewEpg: View
    private lateinit var viewNumericOsd: View

    // Adapters
    private lateinit var hubChannelAdapter: ChannelCardAdapter
    private lateinit var epgChannelAdapter: EpgChannelAdapter
    private lateinit var epgProgramAdapter: EpgProgramAdapter

    // Handler & Runnables for auto-dismiss timers
    private val mainHandler = Handler(Looper.getMainLooper())
    private val hideBannerRunnable = Runnable { hideInfoBanner() }
    private val hideFeedbackRunnable = Runnable { tvQuickFeedback.visibility = View.GONE }
    private val commitNumericChannelRunnable = Runnable { commitNumericChannelInput() }

    // Numeric keypad accumulation
    private val numericInputBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep screen on & immersive TV mode
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUi()

        channelRepository = (application as LiveTvApplication).channelRepository
        playerManager = LiveTvPlayerManager(this)

        initViews()
        setupPlayer()
        setupSmartHub()
        setupEpg()

        // Start playing the last watched channel immediately
        val initialChannel = channelRepository.getLastWatchedChannel()
        tuneToChannel(initialChannel, showBanner = true)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        playerManager.resume()
    }

    override fun onPause() {
        super.onPause()
        playerManager.pause()
        currentChannel?.let { channelRepository.saveLastWatchedChannelId(it.id) }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        playerManager.release()
    }

    private fun hideSystemUi() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }

    private fun initViews() {
        playerView = findViewById(R.id.player_view)
        layoutBuffering = findViewById(R.id.layout_buffering)
        tvBufferingMsg = findViewById(R.id.tv_buffering_msg)
        tvQuickFeedback = findViewById(R.id.tv_quick_feedback)

        viewInfoBanner = findViewById(R.id.view_info_banner)
        viewSmartHub = findViewById(R.id.view_smart_hub)
        viewEpg = findViewById(R.id.view_epg)
        viewNumericOsd = findViewById(R.id.view_numeric_osd)
    }

    private fun setupPlayer() {
        playerManager.attachPlayerView(playerView)

        playerManager.onPlaybackStateChanged = { isBuffering, isPlaying ->
            layoutBuffering.visibility = if (isBuffering) View.VISIBLE else View.GONE
        }

        playerManager.onError = { errorMsg ->
            layoutBuffering.visibility = View.VISIBLE
            tvBufferingMsg.text = errorMsg
        }
    }

    private fun tuneToChannel(channel: Channel, showBanner: Boolean = true) {
        currentChannel = channel
        playerManager.playChannel(channel)
        channelRepository.saveLastWatchedChannelId(channel.id)

        if (showBanner) {
            showInfoBanner(channel)
        }

        hubChannelAdapter.updateChannels(channelRepository.getAllChannels(), channel.id)
    }

    // ==========================================
    // SAMSUNG TIZEN INFO BANNER (Now & Next)
    // ==========================================
    private fun showInfoBanner(channel: Channel) {
        mainHandler.removeCallbacks(hideBannerRunnable)

        val tvNumber = viewInfoBanner.findViewById<TextView>(R.id.tv_channel_number)
        val ivLogo = viewInfoBanner.findViewById<ImageView>(R.id.iv_channel_logo)
        val tvName = viewInfoBanner.findViewById<TextView>(R.id.tv_channel_name)
        val tvCurrentProg = viewInfoBanner.findViewById<TextView>(R.id.tv_current_program)
        val tvProgTime = viewInfoBanner.findViewById<TextView>(R.id.tv_program_time)
        val pbProg = viewInfoBanner.findViewById<ProgressBar>(R.id.progress_program)
        val tvNextProg = viewInfoBanner.findViewById<TextView>(R.id.tv_next_program)
        val badgeRes = viewInfoBanner.findViewById<TextView>(R.id.badge_resolution)
        val badgeHdr = viewInfoBanner.findViewById<TextView>(R.id.badge_hdr)
        val badgeAudio = viewInfoBanner.findViewById<TextView>(R.id.badge_audio)

        tvNumber.text = channel.formattedNumber
        tvName.text = channel.name
        badgeRes.text = channel.resolution
        badgeHdr.text = channel.hdrSupport
        badgeAudio.text = channel.audioFormat

        Glide.with(this)
            .load(channel.logoUrl)
            .placeholder(R.drawable.ic_tizen_tv_logo)
            .error(R.drawable.ic_tizen_tv_logo)
            .into(ivLogo)

        val currentProg = channel.currentProgram
        if (currentProg != null) {
            tvCurrentProg.text = currentProg.title
            tvProgTime.text = currentProg.timeRangeFormatted
            pbProg.progress = currentProg.progressPercent
            pbProg.visibility = View.VISIBLE
        } else {
            tvCurrentProg.text = "Siaran Langsung"
            tvProgTime.text = ""
            pbProg.visibility = View.GONE
        }

        val nextProg = channel.nextProgram
        if (nextProg != null) {
            tvNextProg.text = "Selanjutnya (${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nextProg.startTimeMs))}): ${nextProg.title}"
            tvNextProg.visibility = View.VISIBLE
        } else {
            tvNextProg.visibility = View.GONE
        }

        viewInfoBanner.visibility = View.VISIBLE
        viewInfoBanner.alpha = 0f
        viewInfoBanner.translationY = 60f
        viewInfoBanner.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(260)
            .start()

        // Auto dismiss after 4.5 seconds
        mainHandler.postDelayed(hideBannerRunnable, 4500)
    }

    private fun hideInfoBanner() {
        if (viewInfoBanner.visibility == View.VISIBLE) {
            viewInfoBanner.animate()
                .alpha(0f)
                .translationY(60f)
                .setDuration(220)
                .withEndAction {
                    viewInfoBanner.visibility = View.GONE
                }
                .start()
        }
    }

    // ==========================================
    // SAMSUNG SMART HUB (Bottom Horizontal Bar)
    // ==========================================
    private fun setupSmartHub() {
        val rvHub = viewSmartHub.findViewById<RecyclerView>(R.id.rv_hub_channels)
        rvHub.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        hubChannelAdapter = ChannelCardAdapter(
            channels = channelRepository.getAllChannels(),
            onChannelClick = { channel ->
                tuneToChannel(channel, showBanner = true)
                hideSmartHub()
            }
        )
        rvHub.adapter = hubChannelAdapter

        // Quick Action Buttons
        viewSmartHub.findViewById<Button>(R.id.btn_action_all).setOnClickListener {
            hubChannelAdapter.updateChannels(channelRepository.getAllChannels(), currentChannel?.id)
            showQuickFeedback("Menampilkan Semua Saluran")
        }

        viewSmartHub.findViewById<Button>(R.id.btn_action_epg).setOnClickListener {
            hideSmartHub()
            showEpg()
        }

        viewSmartHub.findViewById<Button>(R.id.btn_action_favorites).setOnClickListener {
            val favs = channelRepository.getChannelsByGenre(ChannelGenre.FAVORITES)
            if (favs.isEmpty()) {
                showQuickFeedback("Belum ada saluran favorit")
            } else {
                hubChannelAdapter.updateChannels(favs, currentChannel?.id)
                showQuickFeedback("Saluran Favorit (${favs.size})")
            }
        }

        viewSmartHub.findViewById<Button>(R.id.btn_action_ratio).setOnClickListener {
            val mode = playerManager.cycleAspectRatio()
            showQuickFeedback(mode)
        }

        viewSmartHub.findViewById<Button>(R.id.btn_action_add_m3u).setOnClickListener {
            hideSmartHub()
            M3uImportDialog(this) { m3uContent ->
                val count = channelRepository.addCustomM3uChannels(m3uContent)
                showQuickFeedback("Berhasil menambahkan $count saluran IPTV!")
                hubChannelAdapter.updateChannels(channelRepository.getAllChannels(), currentChannel?.id)
            }.show()
        }

        viewSmartHub.findViewById<Button>(R.id.btn_action_settings).setOnClickListener {
            hideSmartHub()
            QuickSettingsDialog(
                context = this,
                onCycleAspectRatio = { playerManager.cycleAspectRatio() },
                onResetChannels = {
                    channelRepository.resetToDefaults()
                    hubChannelAdapter.updateChannels(channelRepository.getAllChannels())
                    val first = channelRepository.getAllChannels().first()
                    tuneToChannel(first, showBanner = true)
                }
            ).show()
        }
    }

    private fun toggleSmartHub() {
        if (viewSmartHub.visibility == View.VISIBLE) {
            hideSmartHub()
        } else {
            showSmartHub()
        }
    }

    private fun showSmartHub() {
        hideInfoBanner()
        hideEpg()

        // Update clock in smart hub
        val tvClock = viewSmartHub.findViewById<TextView>(R.id.tv_hub_clock)
        val timeFormat = SimpleDateFormat("HH:mm 'WIB'", Locale.getDefault())
        tvClock.text = timeFormat.format(Date())

        viewSmartHub.visibility = View.VISIBLE
        viewSmartHub.translationY = 200f
        viewSmartHub.alpha = 0f
        viewSmartHub.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(280)
            .start()

        // Focus on channels carousel
        val rvHub = viewSmartHub.findViewById<RecyclerView>(R.id.rv_hub_channels)
        rvHub.post {
            val idx = currentChannel?.let { channelRepository.getAllChannels().indexOf(it) } ?: 0
            rvHub.scrollToPosition(idx)
            rvHub.requestFocus()
        }
    }

    private fun hideSmartHub() {
        if (viewSmartHub.visibility == View.VISIBLE) {
            viewSmartHub.animate()
                .translationY(200f)
                .alpha(0f)
                .setDuration(220)
                .withEndAction {
                    viewSmartHub.visibility = View.GONE
                }
                .start()
        }
    }

    // ==========================================
    // SAMSUNG FULLSCREEN EPG GUIDE
    // ==========================================
    private fun setupEpg() {
        val rvCategories = viewEpg.findViewById<RecyclerView>(R.id.rv_epg_categories)
        val rvChannels = viewEpg.findViewById<RecyclerView>(R.id.rv_epg_channels)
        val rvTimeline = viewEpg.findViewById<RecyclerView>(R.id.rv_epg_timeline)

        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvChannels.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvTimeline.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Setup Categories
        val categories = ChannelGenre.entries
        rvCategories.adapter = GenrePillAdapter(categories) { selectedGenre ->
            val filtered = channelRepository.getChannelsByGenre(selectedGenre)
            epgChannelAdapter.updateChannels(filtered)
            if (filtered.isNotEmpty()) {
                updateEpgTimeline(filtered.first())
            }
        }

        // Setup Timeline Program Adapter
        epgProgramAdapter = EpgProgramAdapter(
            programs = emptyList(),
            onProgramSelected = { program ->
                updateProgramDetailsCard(program)
            },
            onProgramClick = { program ->
                val targetChannel = channelRepository.getChannelById(program.channelId)
                if (targetChannel != null) {
                    tuneToChannel(targetChannel, showBanner = true)
                    hideEpg()
                }
            }
        )
        rvTimeline.adapter = epgProgramAdapter

        // Setup Channels Adapter
        epgChannelAdapter = EpgChannelAdapter(
            channels = channelRepository.getAllChannels(),
            onChannelSelected = { channel ->
                updateEpgTimeline(channel)
            },
            onChannelClick = { channel ->
                tuneToChannel(channel, showBanner = true)
                hideEpg()
            }
        )
        rvChannels.adapter = epgChannelAdapter

        // Watch Now button on EPG details card
        viewEpg.findViewById<Button>(R.id.btn_epg_watch).setOnClickListener {
            val selectedChannel = currentChannel ?: return@setOnClickListener
            tuneToChannel(selectedChannel, showBanner = true)
            hideEpg()
        }
    }

    private fun updateEpgTimeline(channel: Channel) {
        epgProgramAdapter.updatePrograms(channel.programs)
        if (channel.programs.isNotEmpty()) {
            val liveProg = channel.currentProgram ?: channel.programs.first()
            updateProgramDetailsCard(liveProg)
        }
    }

    private fun updateProgramDetailsCard(program: Program) {
        val tvTitle = viewEpg.findViewById<TextView>(R.id.tv_detail_title)
        val tvTime = viewEpg.findViewById<TextView>(R.id.tv_detail_time)
        val tvDesc = viewEpg.findViewById<TextView>(R.id.tv_detail_desc)

        tvTitle.text = program.title
        tvTime.text = "${program.timeRangeFormatted} (${program.durationFormatted}) • ${program.genre} • ${program.videoQuality} • ${program.audioQuality}"
        tvDesc.text = program.description
    }

    private fun showEpg() {
        hideInfoBanner()
        hideSmartHub()

        val tvClock = viewEpg.findViewById<TextView>(R.id.tv_epg_clock)
        val sdf = SimpleDateFormat("EEEE, HH:mm 'WIB'", Locale.getDefault())
        tvClock.text = sdf.format(Date())

        viewEpg.visibility = View.VISIBLE
        viewEpg.alpha = 0f
        viewEpg.animate().alpha(1f).setDuration(250).start()

        val active = currentChannel ?: channelRepository.getAllChannels().first()
        epgChannelAdapter.updateChannels(channelRepository.getAllChannels())
        updateEpgTimeline(active)

        val rvChannels = viewEpg.findViewById<RecyclerView>(R.id.rv_epg_channels)
        rvChannels.requestFocus()
    }

    private fun hideEpg() {
        if (viewEpg.visibility == View.VISIBLE) {
            viewEpg.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    viewEpg.visibility = View.GONE
                }
                .start()
        }
    }

    // ==========================================
    // NUMERIC REMOTE KEYPAD ENTRY (Samsung OSD)
    // ==========================================
    private fun handleNumericKey(digit: Char) {
        mainHandler.removeCallbacks(commitNumericChannelRunnable)

        if (numericInputBuffer.length >= 3) {
            numericInputBuffer.clear()
        }
        numericInputBuffer.append(digit)

        val tvDigits = viewNumericOsd.findViewById<TextView>(R.id.tv_osd_digits)
        val tvHint = viewNumericOsd.findViewById<TextView>(R.id.tv_osd_channel_hint)

        val currentStr = numericInputBuffer.toString()
        tvDigits.text = currentStr.padStart(3, '0')

        val matchedChannel = channelRepository.getChannelByNumber(currentStr)
        tvHint.text = if (matchedChannel != null) {
            "Pindah ke ${matchedChannel.name}"
        } else {
            "Saluran $currentStr..."
        }

        viewNumericOsd.visibility = View.VISIBLE

        if (numericInputBuffer.length >= 3) {
            // Commit immediately if 3 digits entered
            mainHandler.postDelayed(commitNumericChannelRunnable, 400)
        } else {
            // Commit after 1.2s inactivity
            mainHandler.postDelayed(commitNumericChannelRunnable, 1200)
        }
    }

    private fun commitNumericChannelInput() {
        val inputStr = numericInputBuffer.toString()
        numericInputBuffer.clear()
        viewNumericOsd.visibility = View.GONE

        if (inputStr.isNotEmpty()) {
            val target = channelRepository.getChannelByNumber(inputStr)
            if (target != null) {
                tuneToChannel(target, showBanner = true)
            } else {
                showQuickFeedback("Saluran $inputStr tidak ditemukan")
            }
        }
    }

    private fun showQuickFeedback(message: String) {
        mainHandler.removeCallbacks(hideFeedbackRunnable)
        tvQuickFeedback.text = message
        tvQuickFeedback.visibility = View.VISIBLE
        tvQuickFeedback.alpha = 0f
        tvQuickFeedback.animate().alpha(1f).setDuration(180).start()
        mainHandler.postDelayed(hideFeedbackRunnable, 2600)
    }

    // ==========================================
    // TV REMOTE HARDWARE KEY EVENTS DISPATCHER
    // ==========================================
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Direct Numeric Keys (0 - 9)
        if (keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) {
            val digit = ('0' + (keyCode - KeyEvent.KEYCODE_0))
            handleNumericKey(digit)
            return true
        }

        // TV Channel UP / DOWN hardware keys
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_PAGE_UP) {
            val cur = currentChannel ?: return super.onKeyDown(keyCode, event)
            val next = channelRepository.getNextChannel(cur)
            tuneToChannel(next, showBanner = true)
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN || keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            val cur = currentChannel ?: return super.onKeyDown(keyCode, event)
            val prev = channelRepository.getPreviousChannel(cur)
            tuneToChannel(prev, showBanner = true)
            return true
        }

        // Dedicated GUIDE / EPG key on TV remote
        if (keyCode == KeyEvent.KEYCODE_GUIDE || keyCode == KeyEvent.KEYCODE_PROG_RED) {
            if (viewEpg.visibility == View.VISIBLE) hideEpg() else showEpg()
            return true
        }

        // Dedicated INFO key on TV remote
        if (keyCode == KeyEvent.KEYCODE_INFO || keyCode == KeyEvent.KEYCODE_WINDOW) {
            currentChannel?.let { showInfoBanner(it) }
            return true
        }

        // D-Pad navigation when NO overlay is open (Full Screen Live Mode)
        val noOverlayVisible = viewSmartHub.visibility != View.VISIBLE && viewEpg.visibility != View.VISIBLE

        if (noOverlayVisible) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    // Quick channel surf UP
                    val cur = currentChannel ?: return super.onKeyDown(keyCode, event)
                    val next = channelRepository.getNextChannel(cur)
                    tuneToChannel(next, showBanner = true)
                    return true
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    // Quick channel surf DOWN
                    val cur = currentChannel ?: return super.onKeyDown(keyCode, event)
                    val prev = channelRepository.getPreviousChannel(cur)
                    tuneToChannel(prev, showBanner = true)
                    return true
                }

                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    // Open Samsung Smart Hub bottom floating bar
                    showSmartHub()
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    // Toggle EPG Guide
                    showEpg()
                    return true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    // Quick cycle aspect ratio
                    val mode = playerManager.cycleAspectRatio()
                    showQuickFeedback(mode)
                    return true
                }
            }
        }

        // Back button handling
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewEpg.visibility == View.VISIBLE) {
                hideEpg()
                return true
            }
            if (viewSmartHub.visibility == View.VISIBLE) {
                hideSmartHub()
                return true
            }
            if (viewInfoBanner.visibility == View.VISIBLE) {
                hideInfoBanner()
                return true
            }

            // If user is at top-level watching TV, show Samsung TV exit dialog
            showExitConfirmationDialog()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this, R.style.Theme_TizenLiveTv)
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_message))
            .setPositiveButton(getString(R.string.btn_yes)) { _, _ ->
                finish()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
}
