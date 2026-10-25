package com.tizen.livetv.ui.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tizen.livetv.R
import com.tizen.livetv.model.Channel

class ChannelCardAdapter(
    private var channels: List<Channel>,
    private val onChannelClick: (Channel) -> Unit,
    private val onChannelFocused: ((Channel) -> Unit)? = null
) : RecyclerView.Adapter<ChannelCardAdapter.ChannelViewHolder>() {

    private var selectedChannelId: String? = null

    fun updateChannels(newChannels: List<Channel>, activeChannelId: String? = null) {
        channels = newChannels
        selectedChannelId = activeChannelId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel_card, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel)
    }

    override fun getItemCount(): Int = channels.size

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumber: TextView = itemView.findViewById(R.id.tv_card_number)
        private val tvLive: TextView = itemView.findViewById(R.id.tv_card_live)
        private val ivLogo: ImageView = itemView.findViewById(R.id.iv_card_logo)
        private val tvName: TextView = itemView.findViewById(R.id.tv_card_name)
        private val tvProgram: TextView = itemView.findViewById(R.id.tv_card_program)
        private val pbProgress: ProgressBar = itemView.findViewById(R.id.pb_card_progress)

        fun bind(channel: Channel) {
            tvNumber.text = channel.formattedNumber
            tvName.text = channel.name

            val prog = channel.currentProgram
            if (prog != null) {
                tvProgram.text = prog.title
                pbProgress.progress = prog.progressPercent
                pbProgress.visibility = View.VISIBLE
            } else {
                tvProgram.text = "Siaran Langsung"
                pbProgress.visibility = View.GONE
            }

            // Load logo via Glide
            Glide.with(itemView.context)
                .load(channel.logoUrl)
                .placeholder(R.drawable.ic_tizen_tv_logo)
                .error(R.drawable.ic_tizen_tv_logo)
                .into(ivLogo)

            // D-Pad Focus animation (Samsung Tizen scale effect)
            itemView.setOnFocusChangeListener { view, hasFocus ->
                animateFocus(view, hasFocus)
                if (hasFocus) {
                    onChannelFocused?.invoke(channel)
                }
            }

            itemView.setOnClickListener {
                onChannelClick(channel)
            }
        }

        private fun animateFocus(view: View, hasFocus: Boolean) {
            val scale = if (hasFocus) 1.08f else 1.0f
            val elevation = if (hasFocus) 16f else 0f

            view.elevation = elevation
            view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(220)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }
    }
}
