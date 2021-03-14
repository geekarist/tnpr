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
import me.cpele.androcommut.core.Leg
import me.cpele.androcommut.core.Place
import me.cpele.androcommut.core.Trip
import me.cpele.androcommut.tripselection.TripSelectionViewModel.*
import java.util.*

class TripSelectionViewModel(
    private val navitiaService: NavitiaService,
    private val tripCache: LruCache<String, Trip>
) : ViewModel(), Component<Intention, State, Consequence> {

    private val _stateLive = MutableLiveData(State())
    override val stateLive: LiveData<State>
        get() = _stateLive

    private val _eventLive = MutableLiveData<Event<Consequence>>()
    override val eventLive: LiveData<Event<Consequence>>
        get() = _eventLive

    override fun dispatch(intention: Intention) {
        when (intention) {
            is Intention.Load -> handle(intention)
            is Intention.Select -> handle(intention)
        }.exhaust()
    }

    private fun handle(intention: Intention.Load) = viewModelScope.launch {
        Log.d(
            javaClass.simpleName,
            "Origin is ${intention.originId}: ${intention.originLabel}, " +
                    "destination is: ${intention.destinationId}: ${intention.destinationLabel}"
        )

        val navitiaOutcome = withContext(Dispatchers.IO) {
            try {
                val response = navitiaService.journeys(
                    BuildConfig.NAVITIA_API_KEY,
                    intention.originId,
                    intention.destinationId
                )
                Outcome.Success(response)
            } catch (t: Throwable) {
                Outcome.Failure(t)
            }
        }

        val models = navitiaOutcome.toModels()

        val state = _stateLive.value
        val newState = state?.copy(trips = models)

        withContext(Dispatchers.Main) {
            _stateLive.value = newState
        }
    }

    private fun handle(intention: Intention.Select) = viewModelScope.launch {
        Log.d(javaClass.simpleName, "Selected trip: ${intention.trip}")

        val tripId = UUID.nameUUIDFromBytes(intention.trip.toString().toByteArray()).toString()

        withContext(Dispatchers.IO) {
            tripCache.put(tripId, intention.trip)
        }

        _eventLive.value = Event(Consequence.OpenTrip(tripId))
    }

    sealed class Intention {
        data class Load(
            val originId: String,
            val originLabel: String,
            val destinationId: String,
            val destinationLabel: String
        ) : Intention()

        data class Select(val trip: Trip) : Intention()
    }

    data class State(
        val trips: List<Trip>? = null
    )

    sealed class Consequence {
        data class OpenTrip(val tripId: String) : Consequence()
    }

}

private fun Outcome<NavitiaJourneysResult>.toModels(): List<Trip> =
    when (this) {
        is Outcome.Success -> value.toModels()
        is Outcome.Failure -> {
            Log.e(javaClass.simpleName, "Journey request failed", error)
            emptyList()
        }
    }.also {
        Log.d(javaClass.simpleName, "Converted models: $it")
    }

private fun NavitiaJourneysResult.toModels(): List<Trip> =
    journeys
        ?.map { remoteJourney -> trip(remoteJourney) }
        .also { Log.d(javaClass.simpleName, "Models: $it") }
        ?: emptyList()

private fun trip(remoteJourney: NavitiaJourney): Trip {
    val remoteSections = remoteJourney.sections
    val legs = remoteSections
        ?.let { withWaitingSectionsPairedToPrevious(it) }
        ?.also { pairs ->
            pairs.map { (section, waiting) -> "${section.type}/${waiting?.type}" }
                .also { strings -> Log.d("TMP", strings.toString()) }
        }
        ?.map { (remoteSection, waitingSection) -> leg(remoteSection, waitingSection) }
    return Trip(legs ?: emptyList())
}

fun withWaitingSectionsPairedToPrevious(
    sections: List<NavitiaSection>
): List<Pair<NavitiaSection, NavitiaSection?>> =
    sections.fold(listOf()) { acc, section ->
        val previousSectionToWaiting = acc.lastOrNull()
        val newAcc = if (previousSectionToWaiting == null) {
            acc + (section to null)
        } else {
            val isSectionWaiting = section.type == "waiting"
            if (isSectionWaiting) {
                val (previousSection, _) = previousSectionToWaiting
                (acc - previousSectionToWaiting) + (previousSection to section)
            } else {
                acc + (section to null)
            }
        }
        newAcc
    }

private fun leg(remoteSection: NavitiaSection, waitingSection: NavitiaSection?): Leg {
    val remoteDuration = remoteSection.duration
    val duration = remoteDuration
        ?: throw IllegalStateException("Duration should not be null for $remoteSection")
    val from = remoteSection.from?.name ?: "Unknown origin"
    val to = remoteSection.to?.name ?: "Unknown destination"
    val originPlace = Place(from)
    val destinationPlace = Place(to)
    return when (remoteSection.type) {
        "transfer" -> {
            connection(remoteSection, duration, originPlace, destinationPlace, waitingSection)
        }
        "street_network", "crow_fly" -> {
            access(remoteSection, duration, originPlace, destinationPlace)
        }
        "public_transport" -> {
            ride(remoteSection, duration, originPlace, destinationPlace)
        }
        else -> throw IllegalStateException("Unknown section type ${remoteSection.type} for $remoteSection")
    }
}

private fun ride(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Leg.Ride {
    val mode = remoteSection.display_informations?.commercial_mode ?: "?"
    val code = remoteSection.display_informations?.code ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Leg.Ride(startTime, durationMs, originPlace, destinationPlace, mode, code)
}

fun parse(dateTimeStr: String?): Date = parseDateTime(dateTimeStr) ?: Date()

private fun access(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Leg.Access {
    val mode = remoteSection.mode ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Leg.Access(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode
    )
}

private fun connection(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place,
    waitingSection: NavitiaSection?
): Leg.Connection {
    val mode = remoteSection.transfer_type ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Leg.Connection(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode,
        waitingSection?.duration
    )
}
