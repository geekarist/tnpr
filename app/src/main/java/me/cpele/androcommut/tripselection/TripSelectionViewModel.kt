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
import me.cpele.androcommut.BuildConfig
import me.cpele.androcommut.NavitiaJourneysResult
import me.cpele.androcommut.NavitiaService
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

        val state = stateLive.value

        val model = state?.copy(trips = navitiaOutcome.toModels())

        withContext(Dispatchers.Main) {
            _stateLive.value = model
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
    }

private fun NavitiaJourneysResult.toModels(): List<Trip> =
    // TODO: Transform Navitia model to App domain (check and convert)
    journeys?.map { remoteJourney ->
        val remoteSections = remoteJourney.sections
        val legs = remoteSections?.map { remoteSection ->
            val remoteDuration = remoteSection.duration
            val duration = remoteDuration?.toString()
                ?: "Unknown duration" // TODO: Extract string resources (not here)
            val from = remoteSection.from?.name ?: "Unknown origin"
            val to = remoteSection.to?.name ?: "Unknown destination"
            val mode = remoteSection.commercial_mode?.name ?: "Unknown mode"
            val code = remoteSection.code ?: "Unknown line"
            Leg(duration, Place(from), Place(to), mode, code)
        }
        Trip(legs ?: emptyList())
    } ?: emptyList()
