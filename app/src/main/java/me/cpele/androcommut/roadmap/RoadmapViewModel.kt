package me.cpele.androcommut.roadmap

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import me.cpele.androcommut.core.Trip

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
    }

    fun load(tripId: String) {
        inputFlow.value = Input.Load(tripId)
    }
}

private sealed class Input {
    object Default : Input()
    data class Load(val tripId: String) : Input()
}

private sealed class Output {
    class RecallTrip(val tripId: String) : Output()
}

private fun process(inputFlow: Flow<Input>): Flow<Output> {
    val loadFlow: Flow<Input.Load> = inputFlow.filterIsInstance<Input.Load>()
    return loadFlow.map {
        val tripId = it.tripId
        Output.RecallTrip(tripId)
    }
}