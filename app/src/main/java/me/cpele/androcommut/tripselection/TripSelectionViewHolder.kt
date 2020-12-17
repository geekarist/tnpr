package me.cpele.androcommut.tripselection

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.cpele.androcommut.R
import me.cpele.androcommut.core.Trip

class TripSelectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(item: Trip?) {

        val segmentsTextView: TextView = itemView.findViewById(R.id.journey_item_segments)
        segmentsTextView.text = item?.legsSummary

        val durationTextView: TextView = itemView.findViewById(R.id.journey_item_duration)
        durationTextView.text = item?.duration.toString()
    }
}