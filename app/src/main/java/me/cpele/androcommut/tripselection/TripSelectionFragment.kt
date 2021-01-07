package me.cpele.androcommut.tripselection

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.CustomApp
import me.cpele.androcommut.R
import me.cpele.androcommut.core.Trip
import me.cpele.androcommut.tripselection.TripSelectionViewModel.Intention

class TripSelectionFragment : Fragment() {

    companion object {
        fun newInstance() = TripSelectionFragment()
    }

    private var listener: Listener? = null

    private val viewModel: TripSelectionViewModel by viewModels {
        ViewModelFactory {
            TripSelectionViewModel(CustomApp.instance.navitiaService, CustomApp.instance.tripCache)
        }
    }

    private var adapter: TripSelectionAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Listener
            ?: throw IllegalStateException(
                "${context::class.qualifiedName} has to implement ${Listener::class.qualifiedName}"
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trip_selection_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TripSelectionAdapter(::onTripSelected)
        val recyclerView: RecyclerView = view.findViewById(R.id.trip_selection_recycler)
        recyclerView.adapter = adapter

        viewModel.stateLive.observe(viewLifecycleOwner) { state ->
            Log.d(javaClass.simpleName, "Received: $state")
            val uiModels = state.trips // TODO: Convert to UI model
            adapter?.submitList(uiModels)
        }

        viewModel.eventLive.observe(viewLifecycleOwner) { event ->
            event.consume {
                render(it)
            }
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

    private fun render(consequence: TripSelectionViewModel.Consequence) {
        when (consequence) {
            is TripSelectionViewModel.Consequence.OpenTrip ->
                listener?.openTrip(this, consequence.tripId)
        }
    }

    private fun onTripSelected(trip: Trip) {
        viewModel.dispatch(Intention.Select(trip))
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }

    interface Listener {
        fun openTrip(fragment: Fragment, tripId: String)
    }
}