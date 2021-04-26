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
import me.cpele.afk.*
import me.cpele.androcommut.*
import me.cpele.androcommut.core.Journey
import me.cpele.androcommut.core.Place
import me.cpele.androcommut.core.Section
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

        val stateBefore = _stateLive.value ?: State()
        val newStateBefore = stateBefore.copy(isRefreshing = true)
        withContext(Dispatchers.Main) { _stateLive.value = newStateBefore }

        val navitiaOutcome = withContext(Dispatchers.IO) {
            try {
                val response = navitiaService.journeys(
                    BuildConfig.NAVITIA_API_KEY,
                    action.originId,
                    action.destinationId
                )
                Outcome.Success(response)
            } catch (t: Throwable) {
                Outcome.Failure(t)
            }
        }

        val models = navitiaOutcome.toModels()

        val state = _stateLive.value ?: State()
        val newState = state.copy(journeys = models, isRefreshing = false)
        withContext(Dispatchers.Main) { _stateLive.value = newState }
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

private fun Outcome<NavitiaJourneysResult>.toModels(): List<Journey> =
    when (this) {
        is Outcome.Success -> value.toModels()
        is Outcome.Failure -> {
            Log.e(javaClass.simpleName, "Journey request failed", error)
            emptyList()
        }
    }.also {
        Log.d(javaClass.simpleName, "Converted models: $it")
    }

private fun NavitiaJourneysResult.toModels(): List<Journey> =
    journeys
        ?.map { remoteJourney -> journey(remoteJourney) }
        .also { Log.d(javaClass.simpleName, "Models: $it") }
        ?: emptyList()

private fun journey(remoteJourney: NavitiaJourney): Journey {
    val remoteSections = remoteJourney.sections
    val sections = remoteSections
        ?.map { remoteSection -> section(remoteSection) }
    return Journey(sections ?: emptyList())
}

private fun section(remoteSection: NavitiaSection): Section {
    val remoteDuration = remoteSection.duration
    val duration = remoteDuration
        ?: throw IllegalStateException("Duration should not be null for $remoteSection")
    val from = remoteSection.from?.name ?: "Unknown origin"
    val to = remoteSection.to?.name ?: "Unknown destination"
    val originPlace = Place(from)
    val destinationPlace = Place(to)
    return when (remoteSection.type) {
        "transfer" -> {
            transfer(remoteSection, duration, originPlace, destinationPlace)
        }
        "waiting" -> {
            wait(remoteSection, duration)
        }
        "street_network", "crow_fly" -> {
            access(remoteSection, duration, originPlace, destinationPlace)
        }
        "public_transport" -> {
            publicTransport(remoteSection, duration, originPlace, destinationPlace)
        }
        else -> throw IllegalStateException("Unknown section type ${remoteSection.type} for $remoteSection")
    }
}

fun wait(remoteSection: NavitiaSection, duration: Long): Section {
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Wait(duration, startTime)
}

private fun publicTransport(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.PublicTransport {
    val mode = remoteSection.display_informations?.commercial_mode ?: "?"
    val code = remoteSection.display_informations?.code ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Move.PublicTransport(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode,
        code
    )
}

fun parse(dateTimeStr: String?): Date = parseDateTime(dateTimeStr) ?: Date()

private fun access(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.Access {
    val mode = remoteSection.mode ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Move.Access(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode
    )
}

private fun transfer(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.Transfer {
    val mode = remoteSection.transfer_type ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Move.Transfer(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode
    )
}
