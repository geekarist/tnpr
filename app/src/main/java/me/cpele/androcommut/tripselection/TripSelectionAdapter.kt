package me.cpele.androcommut.tripselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import me.cpele.androcommut.R
import me.cpele.androcommut.core.Trip
import kotlin.time.ExperimentalTime

class TripSelectionAdapter : ListAdapter<Trip, TripSelectionViewHolder>(
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
        holder.bind(getItem(position))
    }
}