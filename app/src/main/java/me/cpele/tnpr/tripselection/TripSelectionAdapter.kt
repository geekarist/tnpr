package me.cpele.tnpr.tripselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import me.cpele.tnpr.R
import me.cpele.tnpr.core.Journey
import kotlin.time.ExperimentalTime

class TripSelectionAdapter(private val onItemClickListener: (Journey) -> Unit) :
    ListAdapter<Journey, TripSelectionViewHolder>(
        TripSelectionDiffCallback
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripSelectionViewHolder {
        val layout = R.layout.journey_item_view
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(layout, parent, false)
        return TripSelectionViewHolder(view)
    }

    @ExperimentalTime
    override fun onBindViewHolder(holder: TripSelectionViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClickListener)
    }
}