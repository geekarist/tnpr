package me.cpele.androcommut.roadmap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import me.cpele.afk.Outcome
import me.cpele.afk.ViewModelFactory
import me.cpele.androcommut.CustomApp
import me.cpele.androcommut.R
import me.cpele.androcommut.core.Leg
import me.cpele.androcommut.core.Trip
import kotlin.time.ExperimentalTime

@ExperimentalTime
class RoadmapFragment : Fragment() {

    private val viewModel: RoadmapViewModel by viewModels {
        ViewModelFactory {
            RoadmapViewModel(
                CustomApp.instance.tripCache
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.roadmap_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = RoadmapAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.roadmap_recycler)
        recyclerView.adapter = adapter
        viewModel.state.observe(viewLifecycleOwner) { state ->
            Log.d(javaClass.simpleName, "State: $state")
            val items = items(state)
            adapter.submitList(items)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args =
            arguments?.let { RoadmapFragmentArgs.fromBundle(it) } ?: throw IllegalArgumentException(
                "Invalid arguments: $arguments"
            )
        viewModel.load(args.tripId)
    }
}

private fun items(state: Output.State?): List<RoadmapAdapter.Item> =
    when (val outcome = state?.tripOutcome) {
        is Outcome.Success -> items(outcome.value)
        is Outcome.Failure -> emptyList()
        null -> emptyList()
    }

fun items(trip: Trip): List<RoadmapAdapter.Item> = trip.legs.map { leg -> item(leg) }

fun item(leg: Leg): RoadmapAdapter.Item =
    RoadmapAdapter.Item(
        description = leg.toString(),
        duration = leg.duration
    )
