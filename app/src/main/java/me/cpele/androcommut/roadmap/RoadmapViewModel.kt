@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.cpele.androcommut.roadmap

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.cpele.afk.Outcome
import me.cpele.androcommut.core.Trip
import kotlin.time.ExperimentalTime

@ExperimentalTime
class RoadmapViewModel(private val tripCache: LruCache<String, Trip>) : ViewModel() {

    private val inputFlow = MutableStateFlow<Input>(Input.Default)

    private val outputFlow: Flow<Output> = process(inputFlow)

    val state: LiveData<Output.State> =
        outputFlow.filterIsInstance<Output.State>().asLiveData()

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
        inputFlow.value = Input.Start(tripId)
    }
}

private sealed class Input {
    object Default : Input()
    data class Start(val tripId: String) : Input()
    data class TripRecalled(val id: String, val trip: Trip?) : Input()
}

sealed class Output { // TODO: Make private
    data class RecallTrip(val tripId: String) : Output()
    data class State(val tripOutcome: Outcome<Trip>) : Output()
}

@ExperimentalTime
private fun process(inputFlow: Flow<Input>): Flow<Output> {

    // Start ⇒ recall trip
    val startFlow = inputFlow.filterIsInstance<Input.Start>()
    val recallTripFlow = startFlow.map {
        val tripId = it.tripId
        Output.RecallTrip(tripId)
    }

    // Trip recalled ⇒ change state
    val tripRecalledFlow = inputFlow.filterIsInstance<Input.TripRecalled>()
    val changeStateFlow = tripRecalledFlow.map { recalled ->
        val trip = recalled.trip
        val tripId = recalled.id
        val outcome = if (trip == null) {
            Outcome.Failure(Exception("Trip not found: $tripId"))
        } else {
            Outcome.Success(trip)
        }
        Output.State(outcome)
    }

    return merge(recallTripFlow, changeStateFlow)
}