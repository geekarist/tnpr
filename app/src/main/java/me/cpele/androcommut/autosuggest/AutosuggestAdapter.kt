package me.cpele.androcommut.autosuggest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import me.cpele.androcommut.R

class AutosuggestAdapter(private val onItemClickListener: (PlaceUiModel) -> Unit) :
    ListAdapter<PlaceUiModel, AutosuggestViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutosuggestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.autosuggest_result_view, parent, false)
        return AutosuggestViewHolder(view)
    }

    override fun onBindViewHolder(holder: AutosuggestViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClickListener(item)
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<PlaceUiModel>() {

        override fun areItemsTheSame(
            oldItem: PlaceUiModel,
            newItem: PlaceUiModel
        ) = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: PlaceUiModel,
            newItem: PlaceUiModel
        ) = oldItem == newItem
    }
}
