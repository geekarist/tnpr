package me.cpele.androcommut.tripselection

import android.util.Log
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
import me.cpele.androcommut.*
import me.cpele.androcommut.core.Leg
import me.cpele.androcommut.core.Place
import me.cpele.androcommut.core.Trip
import me.cpele.androcommut.tripselection.TripSelectionViewModel.*

class TripSelectionViewModel(
    private val navitiaService: NavitiaService
) : ViewModel(), Component<Intention, State, Consequence> {

    private val _stateLive = MutableLiveData(State())
    override val stateLive: LiveData<State>
        get() = _stateLive

    override val eventLive: LiveData<Event<Consequence>>
        get() = TODO("Not yet implemented")

    override fun dispatch(intention: Intention) {
        when (intention) {
            is Intention.Load -> handle(intention)
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

    sealed class Intention {
        data class Load(
            val originId: String,
            val originLabel: String,
            val destinationId: String,
            val destinationLabel: String
        ) : Intention()
    }

    data class State(
        val trips: List<Trip>? = null
    )

    sealed class Consequence {
    }

}

private fun Outcome<NavitiaJourneysResult>.toModels(): List<Trip> =
    when (this) {
        is Outcome.Success -> value.toModels()
        is Outcome.Failure -> emptyList()
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
    val legs = remoteSections?.map { remoteSection -> leg(remoteSection) }
    return Trip(legs ?: emptyList())
}

private fun leg(remoteSection: NavitiaSection): Leg {
    val remoteDuration = remoteSection.duration
    val duration = remoteDuration?.toString() ?: "Unknown duration"
    val from = remoteSection.from?.name ?: "Unknown origin"
    val to = remoteSection.to?.name ?: "Unknown destination"
    val originPlace = Place(from)
    val destinationPlace = Place(to)
    return when (remoteSection.type) {
        "transfer", "waiting" -> {
            connection(remoteSection, duration, originPlace, destinationPlace)
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
    duration: String,
    originPlace: Place,
    destinationPlace: Place
): Leg.Ride {
    val mode = remoteSection.display_informations?.commercial_mode ?: "?"
    val code = remoteSection.display_informations?.code ?: "?"
    return Leg.Ride(duration, originPlace, destinationPlace, mode, code)
}

private fun access(
    remoteSection: NavitiaSection,
    duration: String,
    originPlace: Place,
    destinationPlace: Place
): Leg.Access {
    val mode = remoteSection.mode ?: "?"
    return Leg.Access(
        duration,
        originPlace,
        destinationPlace,
        mode
    )
}

private fun connection(
    remoteSection: NavitiaSection,
    duration: String,
    originPlace: Place,
    destinationPlace: Place
): Leg.Connection {
    val mode = remoteSection.mode ?: "?"
    return Leg.Connection(
        duration,
        originPlace,
        destinationPlace,
        mode
    )
}
