package me.cpele.tnpr.tripselection

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.cpele.tnpr.R
import me.cpele.tnpr.core.Journey
import kotlin.time.ExperimentalTime

class TripSelectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @ExperimentalTime
    fun bind(item: Journey, onItemClickListener: (Journey) -> Unit) {

        val segmentsTextView: TextView = itemView.findViewById(R.id.journey_item_segments)
        segmentsTextView.text = item.sectionsSummary

        val durationTextView: TextView = itemView.findViewById(R.id.journey_item_duration)
        durationTextView.text = item.formattedDuration

        val fromTextView: TextView = itemView.findViewById(R.id.journey_item_start)
        fromTextView.text = itemView.context.getString(R.string.trip_from, item.originName)

        val toTextView: TextView = itemView.findViewById(R.id.journey_item_end)
        toTextView.text = itemView.context.getString(R.string.trip_to, item.destinationName)

        itemView.setOnClickListener {
            onItemClickListener(item)
        }
    }
}