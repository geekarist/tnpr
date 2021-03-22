@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.cpele.androcommut.roadmap

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.cpele.afk.Outcome
import me.cpele.androcommut.core.Journey
import kotlin.time.ExperimentalTime

@ExperimentalTime
class RoadmapViewModel(private val journeyCache: LruCache<String, Journey>) : ViewModel() {

    private val inputFlow = MutableStateFlow<Input>(Input.Default)

    private val outputFlow: Flow<Output> = process(inputFlow)

    val state: LiveData<State> get() = _state
    private val _state = MutableLiveData<State>()

    data class State(val journeyOutcome: Outcome<Journey>)

    init {
        outputFlow.onEach {
            processOutput(it)
        }.launchIn(viewModelScope)
    }

    private fun processOutput(output: Output) {
        when (output) {
            is Output.RecallTrip -> recallTrip(output.tripId)
            is Output.ChangeState -> changeState(output.journeyOutcome)
        }
    }

    private fun changeState(journeyOutcome: Outcome<Journey>) {
        _state.value = State(journeyOutcome)
    }

    private fun recallTrip(tripId: String) {
        val recalled = journeyCache.get(tripId)
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
    data class TripRecalled(val id: String, val journey: Journey?) : Input()
}

private sealed class Output { // TODO: Make private
    data class RecallTrip(val tripId: String) : Output()
    data class ChangeState(val journeyOutcome: Outcome<Journey>) : Output()
}

@ExperimentalTime
private fun process(inputFlow: Flow<Input>): Flow<Output> = merge(

    // Start ⇒ recall trip
    inputFlow.filterIsInstance<Input.Start>()
        .map {
            val tripId = it.tripId
            Output.RecallTrip(tripId)
        },

    // Trip recalled ⇒ change state
    inputFlow.filterIsInstance<Input.TripRecalled>()
        .map { recalled ->
            val trip = recalled.journey
            val tripId = recalled.id
            val outcome = if (trip == null) {
                Outcome.Failure(Exception("Trip not found: $tripId"))
            } else {
                Outcome.Success(trip)
            }
            Output.ChangeState(outcome)
        }
)