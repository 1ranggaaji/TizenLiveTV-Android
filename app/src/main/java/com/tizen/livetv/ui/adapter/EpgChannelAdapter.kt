package com.tizen.livetv.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tizen.livetv.R
import com.tizen.livetv.model.Channel

class EpgChannelAdapter(
    private var channels: List<Channel>,
    private val onChannelSelected: (Channel) -> Unit,
    private val onChannelClick: (Channel) -> Unit
) : RecyclerView.Adapter<EpgChannelAdapter.EpgChannelViewHolder>() {

    fun updateChannels(newChannels: List<Channel>) {
        channels = newChannels
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpgChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_epg_channel, parent, false)
        return EpgChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpgChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.bind(channel)
    }

    override fun getItemCount(): Int = channels.size

    inner class EpgChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumber: TextView = itemView.findViewById(R.id.tv_epg_channel_num)
        private val ivLogo: ImageView = itemView.findViewById(R.id.iv_epg_channel_logo)
        private val tvName: TextView = itemView.findViewById(R.id.tv_epg_channel_name)
        private val ivFav: ImageView = itemView.findViewById(R.id.iv_epg_fav_indicator)

        fun bind(channel: Channel) {
            tvNumber.text = channel.formattedNumber
            tvName.text = channel.name
            ivFav.visibility = if (channel.isFavorite) View.VISIBLE else View.GONE

            Glide.with(itemView.context)
                .load(channel.logoUrl)
                .placeholder(R.drawable.ic_tizen_tv_logo)
                .error(R.drawable.ic_tizen_tv_logo)
                .into(ivLogo)

            itemView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    onChannelSelected(channel)
                }
            }

            itemView.setOnClickListener {
                onChannelClick(channel)
            }
        }
    }
}
