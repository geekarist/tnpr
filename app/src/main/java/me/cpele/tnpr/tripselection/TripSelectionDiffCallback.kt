package me.cpele.tnpr.tripselection

import androidx.recyclerview.widget.DiffUtil
import me.cpele.tnpr.core.Journey

object TripSelectionDiffCallback : DiffUtil.ItemCallback<Journey>() {

    override fun areItemsTheSame(
        oldItem: Journey,
        newItem: Journey
    ): Boolean {
        return false
    }

    override fun areContentsTheSame(
        oldItem: Journey,
        newItem: Journey
    ): Boolean {
        return false
    }
}