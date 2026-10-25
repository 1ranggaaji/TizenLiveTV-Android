package com.tizen.livetv.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tizen.livetv.R
import com.tizen.livetv.model.ChannelGenre

class GenrePillAdapter(
    private val genres: List<ChannelGenre>,
    private val onGenreSelected: (ChannelGenre) -> Unit
) : RecyclerView.Adapter<GenrePillAdapter.GenreViewHolder>() {

    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre_pill, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.bind(genre, position == selectedIndex)
    }

    override fun getItemCount(): Int = genres.size

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPill: TextView = itemView.findViewById(R.id.tv_genre_pill)

        fun bind(genre: ChannelGenre, isSelected: Boolean) {
            tvPill.text = genre.displayName
            tvPill.isSelected = isSelected

            tvPill.setOnClickListener {
                val prev = selectedIndex
                selectedIndex = adapterPosition
                notifyItemChanged(prev)
                notifyItemChanged(selectedIndex)
                onGenreSelected(genre)
            }
        }
    }
}
