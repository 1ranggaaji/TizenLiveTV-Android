package com.tizen.livetv.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tizen.livetv.R
import com.tizen.livetv.model.Program

class EpgProgramAdapter(
    private var programs: List<Program>,
    private val onProgramSelected: (Program) -> Unit,
    private val onProgramClick: (Program) -> Unit
) : RecyclerView.Adapter<EpgProgramAdapter.EpgProgramViewHolder>() {

    fun updatePrograms(newPrograms: List<Program>) {
        programs = newPrograms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpgProgramViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_epg_program, parent, false)
        return EpgProgramViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpgProgramViewHolder, position: Int) {
        val program = programs[position]
        holder.bind(program)
    }

    override fun getItemCount(): Int = programs.size

    inner class EpgProgramViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSlotTime: TextView = itemView.findViewById(R.id.tv_program_slot_time)
        private val tvLiveTag: TextView = itemView.findViewById(R.id.tv_program_live_tag)
        private val tvSlotTitle: TextView = itemView.findViewById(R.id.tv_program_slot_title)
        private val tvSlotDuration: TextView = itemView.findViewById(R.id.tv_program_slot_duration)
        private val pbProgress: ProgressBar = itemView.findViewById(R.id.pb_program_slot_progress)

        fun bind(program: Program) {
            tvSlotTime.text = program.timeRangeFormatted
            tvSlotTitle.text = program.title
            tvSlotDuration.text = "${program.durationFormatted} • ${program.genre}"

            val isLive = program.isLive
            tvLiveTag.visibility = if (isLive) View.VISIBLE else View.GONE
            pbProgress.visibility = if (isLive) View.VISIBLE else View.GONE
            if (isLive) {
                pbProgress.progress = program.progressPercent
            }

            itemView.setOnFocusChangeListener { view, hasFocus ->
                val scale = if (hasFocus) 1.05f else 1.0f
                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator(1.1f))
                    .start()

                if (hasFocus) {
                    onProgramSelected(program)
                }
            }

            itemView.setOnClickListener {
                onProgramClick(program)
            }
        }
    }
}
