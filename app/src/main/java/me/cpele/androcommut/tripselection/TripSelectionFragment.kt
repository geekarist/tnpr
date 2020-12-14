package me.cpele.androcommut.tripselection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.CustomApp
import me.cpele.androcommut.R
import me.cpele.androcommut.tripselection.TripSelectionViewModel.Intention

class TripSelectionFragment : Fragment() {

    companion object {
        fun newInstance() = TripSelectionFragment()
    }

    private val viewModel: TripSelectionViewModel by viewModels {
        ViewModelFactory {
            TripSelectionViewModel(CustomApp.instance.navitiaService)
        }
    }

    private val adapter = Adapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trip_selection_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.trip_selection_recycler)
        recyclerView.adapter = adapter

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            Log.d(javaClass.simpleName, "Received: $state")
            val uiModels = state.models
            adapter.submitList(uiModels)
        }

        val intention = arguments
            ?.let { TripSelectionFragmentArgs.fromBundle(it) }
            ?.let {
                Intention.Load(
                    it.originId,
                    it.originLabel,
                    it.destinationId,
                    it.destinationLabel
                )
            }
            ?: throw IllegalArgumentException("Arguments must not be null")

        viewModel.dispatch(intention)
    }
}

private class Adapter : ListAdapter<TripSelectionViewModel.Model, ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = R.layout.journey_item_view
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private object DiffCallback : DiffUtil.ItemCallback<TripSelectionViewModel.Model>() {

    override fun areItemsTheSame(
        oldItem: TripSelectionViewModel.Model,
        newItem: TripSelectionViewModel.Model
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun areContentsTheSame(
        oldItem: TripSelectionViewModel.Model,
        newItem: TripSelectionViewModel.Model
    ): Boolean {
        TODO("Not yet implemented")
    }
}

private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(item: TripSelectionViewModel.Model?) {

        val segmentsTextView: TextView = itemView.findViewById(R.id.journey_item_segments)
        segmentsTextView.text = item?.legsSummary

        val durationTextView: TextView = itemView.findViewById(R.id.journey_item_duration)
        durationTextView.text = item?.duration
    }
}
