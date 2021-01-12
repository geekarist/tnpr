@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.cpele.androcommut.roadmap

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.cpele.androcommut.core.Trip
import kotlin.time.ExperimentalTime

@ExperimentalTime
class RoadmapViewModel(private val tripCache: LruCache<String, Trip>) : ViewModel() {

    private val inputFlow = MutableStateFlow<Input>(Input.Default)

    private val outputFlow: Flow<Output> = process(inputFlow)

    init {
        outputFlow.onEach {
            processOutput(it)
        }.launchIn(viewModelScope)
    }

    private fun processOutput(output: Output) {
        when (output) {
            is Output.RecallTrip -> recallTrip(output.tripId)
        }
    }

    private fun recallTrip(tripId: String) {
        val recalled = tripCache.get(tripId)
        Log.d(javaClass.simpleName, "Recalled trip: $recalled")
        inputFlow.value = Input.TripRecalled(tripId, recalled)
    }

    fun load(tripId: String) {
        inputFlow.value = Input.Load(tripId)
    }
}

private sealed class Input {
    object Default : Input()
    data class Load(val tripId: String) : Input()
    data class TripRecalled(val id: String, val trip: Trip?) : Input()
}

private sealed class Output {
    data class RecallTrip(val tripId: String) : Output()
    data class State(
        val segments: List<SegmentUiModel> = emptyList(),
        val error: ErrorUiModel? = null
    ) : Output()
}

data class SegmentUiModel(val description: CharSequence, val duration: CharSequence)

data class ErrorUiModel(val message: CharSequence)

@ExperimentalTime
private fun process(inputFlow: Flow<Input>): Flow<Output> {

    // Input
    val loadFlow = inputFlow.filterIsInstance<Input.Load>()
    val tripRecalled = inputFlow.filterIsInstance<Input.TripRecalled>()

    // State
    val stateFlow = tripRecalled.map { recalled ->
        val trip = recalled.trip
        val tripId = recalled.id
        if (trip == null) {
            Output.State(error = ErrorUiModel("Trip not found: $tripId"))
        } else {
            Output.State(
                segments = listOf(
                    SegmentUiModel(
                        trip.legsSummary,
                        trip.formattedDuration
                    )
                )
            )
        }
    }

    // Output
    val recallTripFlow = loadFlow.map {
        val tripId = it.tripId
        Output.RecallTrip(tripId)
    }
    return merge(recallTripFlow, stateFlow)
}