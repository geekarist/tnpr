package me.cpele.androcommut.tripselection

import androidx.recyclerview.widget.DiffUtil
import me.cpele.androcommut.core.Trip

object TripSelectionDiffCallback : DiffUtil.ItemCallback<Trip>() {

    override fun areItemsTheSame(
        oldItem: Trip,
        newItem: Trip
    ): Boolean {
        return false
    }

    override fun areContentsTheSame(
        oldItem: Trip,
        newItem: Trip
    ): Boolean {
        return false
    }
}