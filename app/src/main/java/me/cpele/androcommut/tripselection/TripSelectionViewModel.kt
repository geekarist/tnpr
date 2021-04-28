package me.cpele.androcommut.tripselection

import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.cpele.afk.Component
import me.cpele.afk.Event
import me.cpele.afk.Outcome
import me.cpele.afk.exhaust
import me.cpele.androcommut.BuildConfig
import me.cpele.androcommut.NavitiaJourneysResult
import me.cpele.androcommut.NavitiaService
import me.cpele.androcommut.core.Journey
import me.cpele.androcommut.tripselection.TripSelectionViewModel.*
import java.util.*

class TripSelectionViewModel(
    private val navitiaService: NavitiaService,
    private val journeyCache: LruCache<String, Journey>
) : ViewModel(), Component<Action, State, Consequence> {

    private val _stateLive = MutableLiveData(State(isRefreshing = true))
    override val stateLive: LiveData<State>
        get() = _stateLive

    private val _eventLive = MutableLiveData<Event<Consequence>>()
    override val eventLive: LiveData<Event<Consequence>>
        get() = _eventLive

    override fun dispatch(action: Action) {
        when (action) {
            is Action.Load -> handle(action)
            is Action.Select -> handle(action)
        }.exhaust()
    }

    private fun handle(action: Action.Load) = viewModelScope.launch {
        Log.d(
            javaClass.simpleName,
            "Origin is ${action.originId}: ${action.originLabel}, " +
                    "destination is: ${action.destinationId}: ${action.destinationLabel}"
        )

        // Indicate ref:resh
        val stateBefore = _stateLive.value ?: State()
        val newStateBefore = stateBefore.copy(isRefreshing = true)
        withContext(Dispatchers.Main) { _stateLive.value = newStateBefore }

        // Fetch then model
        val navitiaOutcome = fetchJourneys(action.originId, action.destinationId)
        val models = model(navitiaOutcome)

        // Update state
        val state = _stateLive.value ?: State()
        val newState = state.copy(journeys = models, isRefreshing = false)
        withContext(Dispatchers.Main) { _stateLive.value = newState }
    }

    private suspend fun fetchJourneys(
        originId: String,
        destinationId: String
    ): Outcome<NavitiaJourneysResult> = withContext(Dispatchers.IO) {
        try {
            val response = navitiaService.journeys(
                BuildConfig.NAVITIA_API_KEY,
                originId,
                destinationId
            )
            Outcome.Success(response)
        } catch (t: Throwable) {
            Outcome.Failure(t)
        }
    }

    private fun handle(action: Action.Select) = viewModelScope.launch {
        Log.d(javaClass.simpleName, "Selected trip: ${action.journey}")

        val tripId = UUID.nameUUIDFromBytes(action.journey.toString().toByteArray()).toString()

        withContext(Dispatchers.IO) {
            journeyCache.put(tripId, action.journey)
        }

        _eventLive.value = Event(Consequence.OpenTrip(tripId))
    }

    sealed class Action {
        data class Load(
            val originId: String,
            val originLabel: String,
            val destinationId: String,
            val destinationLabel: String
        ) : Action()

        data class Select(val journey: Journey) : Action()
    }

    data class State(
        val journeys: List<Journey>? = null,
        val isRefreshing: Boolean? = null
    )

    sealed class Consequence {
        data class OpenTrip(val tripId: String) : Consequence()
    }
}
