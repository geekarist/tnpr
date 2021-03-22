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
import me.cpele.androcommut.core.Journey
import me.cpele.androcommut.core.Section
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class RoadmapFragment : Fragment() {

    private val viewModel: RoadmapViewModel by viewModels {
        ViewModelFactory {
            RoadmapViewModel(
                CustomApp.instance.journeyCache
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
            val items = items(requireContext(), state?.journeyOutcome)
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
private fun items(context: Context, journeyOutcome: Outcome<Journey>?): List<RoadmapAdapter.Item> =
    when (journeyOutcome) {
        is Outcome.Success -> items(context, journeyOutcome.value)
        is Outcome.Failure -> emptyList()
        null -> emptyList()
    }

@ExperimentalTime
fun items(context: Context, journey: Journey): List<RoadmapAdapter.Item> =
    journey.sections.map { leg -> item(context, leg) }

@ExperimentalTime
fun item(context: Context, section: Section): RoadmapAdapter.Item =
    RoadmapAdapter.Item(
        description = description(context, section),
        duration = "${section.durationSec.seconds.inMinutes.roundToInt()} min"
    )

private fun description(context: Context, section: Section): String {
    val startTime: Date = section.startTime
    val formattedStartTime = DateUtils.formatDateTime(
        context,
        startTime.time,
        DateUtils.FORMAT_SHOW_TIME
    )
    return when (section) {
        is Section.Move -> {
            val mode = section.mode
            val start = section.origin.name
            val end = section.destination.name
            when (section) {
                is Section.Move.PublicTransport -> {
                    val line = section.line
                    "At $formattedStartTime, take the $mode $line from $start to $end"
                }
                is Section.Move.Access, is Section.Move.Transfer ->
                    "At $formattedStartTime, go by $mode from $start to $end"
            }
        }
        is Section.Wait -> {
            "At $formattedStartTime, wait"
        }
    }
}