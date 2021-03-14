package me.cpele.androcommut.roadmap

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
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
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

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
            val items = items(requireContext(), state?.tripOutcome)
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

@ExperimentalTime
private fun items(context: Context, tripOutcome: Outcome<Trip>?): List<RoadmapAdapter.Item> =
    when (tripOutcome) {
        is Outcome.Success -> items(context, tripOutcome.value)
        is Outcome.Failure -> emptyList()
        null -> emptyList()
    }

@ExperimentalTime
fun items(context: Context, trip: Trip): List<RoadmapAdapter.Item> =
    trip.legs.map { leg -> item(context, leg) }

@ExperimentalTime
fun item(context: Context, leg: Leg): RoadmapAdapter.Item =
    RoadmapAdapter.Item(
        description = description(context, leg),
        duration = "${leg.durationSec.seconds.inMinutes.roundToInt()} min"
    )

private fun description(context: Context, leg: Leg): String {
    val startTime: Date = leg.startTime
    val formattedStartTime = DateUtils.formatDateTime(
        context,
        startTime.time,
        DateUtils.FORMAT_SHOW_TIME
    )
    val mode = leg.mode
    val start = leg.origin.name
    val end = leg.destination.name
    return when (leg) {
        is Leg.Ride -> {
            val line = leg.line
            "At $formattedStartTime, take the $mode $line from $start to $end"
        }
        is Leg.Access, is Leg.Connection ->
            "At $formattedStartTime, go by $mode from $start to $end" + waitingTimeDesc(leg)
    }
}

fun waitingTimeDesc(leg: Leg): String =
    if (leg is Leg.Connection) {
        val waitingTimeSec = leg.durationSec
        val formattedDuration = DateUtils.formatElapsedTime(waitingTimeSec)
        ", then wait for $formattedDuration"
    } else {
        ""
    }
