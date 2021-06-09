package me.cpele.tnpr.tripselection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.cpele.afk.ViewModelFactory
import me.cpele.tnpr.CustomApp
import me.cpele.tnpr.R
import me.cpele.tnpr.core.Journey
import me.cpele.tnpr.tripselection.TripSelectionViewModel.Action

class TripSelectionFragment : Fragment() {

    private var listener: Listener? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var adapter: TripSelectionAdapter? = null

    private lateinit var viewModel: TripSelectionViewModel

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
    ): View? = inflater.inflate(R.layout.trip_selection_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialAction = arguments
            ?.let { TripSelectionFragmentArgs.fromBundle(it) }
            ?.let {
                Action.Load(
                    it.originId,
                    it.originLabel,
                    it.destinationId,
                    it.destinationLabel
                )
            }
            ?: throw IllegalArgumentException("Arguments must not be null")

        viewModel = ViewModelProvider(this, ViewModelFactory {
            TripSelectionViewModel(
                CustomApp.instance.navitiaService,
                CustomApp.instance.journeyCache,
                CustomApp.instance,
                initialAction
            )
        }).get()

        refreshLayout = view.findViewById(R.id.trip_selection_swipe_refresh)

        adapter = TripSelectionAdapter(::onTripSelected)
        val recyclerView: RecyclerView = view.findViewById(R.id.trip_selection_recycler)
        recyclerView.adapter = adapter

        viewModel.stateLive.observe(viewLifecycleOwner) { state: TripSelectionViewModel.State ->
            render(state)
        }

        viewModel.eventLive.observe(viewLifecycleOwner) { event ->
            event.consume {
                render(it)
            }
        }

        refreshLayout?.setOnRefreshListener { viewModel.dispatch(initialAction) }
    }

    private fun render(state: TripSelectionViewModel.State) {
        state.journeys?.let { journeys ->
            adapter?.submitList(journeys)
        }
        state.isRefreshing?.let {
            refreshLayout?.isRefreshing = it
        }
        val flipper = view?.findViewById<ViewFlipper>(R.id.trip_selection_flipper)
        flipper?.displayedChild = when (state.status) {
            null,
            TripSelectionViewModel.State.Status.SUCCESS -> 0
            TripSelectionViewModel.State.Status.FAILURE -> 1
            TripSelectionViewModel.State.Status.NOT_FOUND -> 2
        }
    }

    private fun render(consequence: TripSelectionViewModel.Consequence) {
        when (consequence) {
            is TripSelectionViewModel.Consequence.OpenTrip ->
                listener?.openTrip(this, consequence.tripId)
        }
    }

    private fun onTripSelected(journey: Journey) {
        viewModel.dispatch(Action.Select(journey))
    }

    override fun onDestroyView() {
        adapter = null
        refreshLayout = null
        super.onDestroyView()
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    interface Listener {
        fun openTrip(fragment: Fragment, tripId: String)
    }
}