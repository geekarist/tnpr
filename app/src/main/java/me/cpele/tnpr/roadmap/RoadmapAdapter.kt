package me.cpele.tnpr.roadmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.cpele.tnpr.R

class RoadmapAdapter : ListAdapter<RoadmapAdapter.Item, RoadmapAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.segment_view,
                    parent,
                    false
                )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    data class Item(val description: CharSequence, val duration: String)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: Item) {
            itemView.findViewById<TextView>(R.id.segment_description).text = item.description
            itemView.findViewById<TextView>(R.id.segment_duration).text = item.duration
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Item>() {

        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return false
        }
    }
}
