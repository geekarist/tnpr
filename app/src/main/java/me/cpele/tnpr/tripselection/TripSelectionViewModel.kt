package me.cpele.tnpr.tripselection

import android.app.Application
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
import me.cpele.tnpr.BuildConfig
import me.cpele.tnpr.NavitiaJourneysResult
import me.cpele.tnpr.NavitiaService
import me.cpele.tnpr.R
import me.cpele.tnpr.core.Journey
import me.cpele.tnpr.tripselection.TripSelectionViewModel.*
import java.util.*

class TripSelectionViewModel(
    private val navitiaService: NavitiaService,
    private val journeyCache: LruCache<String, Journey>,
    private val application: Application,
    initialAction: Action
) : ViewModel(), Component<Action, State, Consequence>, TripSelectionTranslator {

    private val _stateLive = MutableLiveData(State(isRefreshing = true))
    override val stateLive: LiveData<State>
        get() = _stateLive

    private val _eventLive = MutableLiveData<Event<Consequence>>()
    override val eventLive: LiveData<Event<Consequence>>
        get() = _eventLive

    init {
        dispatch(initialAction)
    }

    override fun dispatch(action: Action) {
        when (action) {
            is Action.Load -> handle(action)
            is Action.Select -> handle(action)
        }.exhaust()
    }

    private fun handle(action: Action.Load) = viewModelScope.launch {

        // Indicate refresh
        val newStateBefore = withContext(Dispatchers.Default) {
            _stateLive.value?.copy(isRefreshing = true)
        }
        withContext(Dispatchers.Main) { _stateLive.value = newStateBefore }

        // Fetch then model
        val navitiaOutcome = fetchJourneys(action.originId, action.destinationId)
        val model = withContext(Dispatchers.Default) {
            model(this@TripSelectionViewModel, navitiaOutcome)
        }

        withContext(Dispatchers.Default) {
            model.errors.forEach { err ->
                Log.w(javaClass.simpleName, "Errors fetching journeys", err)
            }
        }

        // Update state
        val newState = withContext(Dispatchers.Default) {
            _stateLive.value?.updatedWith(model)
        }
        withContext(Dispatchers.Main) { _stateLive.value = newState }
    }

    private fun State.updatedWith(
        model: TripSelectionModel
    ): State = copy(
        journeys = model.journeys,
        isRefreshing = false,
        status = when {
            model.journeys.isEmpty() && model.errors.isNotEmpty() -> State.Status.FAILURE
            model.journeys.isEmpty() -> State.Status.NOT_FOUND
            else -> State.Status.SUCCESS
        }
    )

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
        val isRefreshing: Boolean? = null,
        val status: Status? = Status.SUCCESS
    ) {
        enum class Status {
            SUCCESS, FAILURE, NOT_FOUND
        }
    }

    sealed class Consequence {
        data class OpenTrip(val tripId: String) : Consequence()
    }

    override fun processTransferType(transferType: String?): String =
        when (transferType) {
            "walking" -> application.getString(R.string.journey_selection_transfer_type_walking)
            "stay_in" -> application.getString(R.string.journey_selection_transfer_type_stay_in)
            else -> application.getString(R.string.journey_selection_transfer_type_unknown)
        }

    override fun processMode(mode: String?): String? =
        when (mode) {
            "walking" -> application.getString(R.string.roadmap_mode_walking)
            "car" -> application.getString(R.string.roadmap_mode_car)
            "bike" -> application.getString(R.string.roadmap_mode_bike_personal)
            "bss" -> application.getString(R.string.roadmap_mode_bike_sharing)
            "ridesharing" -> application.getString(R.string.roadmap_mode_ride_sharing)
            "taxi" -> application.getString(R.string.roadmap_mode_taxi)
            else -> mode
        }
}
