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

        val model = state?.copy(models = navitiaOutcome.toModels())

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
        val models: List<Model>? = null
    )

    sealed class Consequence {
    }

    data class Model(
        val legs: List<Leg>
    ) {
        data class Leg(
            val duration: String,
            val origin: Place,
            val destination: Place,
            val mode: String,
            val line: String
        )

        val legsSummary: CharSequence? by lazy {
            legs.joinToString(", ") { "${it.mode} ${it.line}" }
        }

        val duration: Int by lazy {
            legs.sumBy { it.duration }
        }

        data class Place(val name: String)
    }
}

private fun Outcome<NavitiaJourneysResult>.toModels(): List<Model> =
    when (this) {
        is Outcome.Success -> value.toModels()
        is Outcome.Failure -> emptyList()
    }

private fun NavitiaJourneysResult.toModels(): List<Model> =
    journeys?.map { remoteJourney ->
        val remoteSections = remoteJourney.sections
        val legs = remoteSections?.map { remoteSection ->
            val remoteDuration = remoteSection.duration
            val duration = remoteDuration?.toString()
                ?: "Unknown duration" // TODO: Extract string resources
            val from = remoteSection.from?.name ?: "Unknown origin"
            val to = remoteSection.to?.name ?: "Unknown destination"
            val mode = remoteSection.commercial_mode?.name ?: "Unknown mode"
            val code = remoteSection.code ?: "Unknown line"
            Model.Leg(duration, Model.Place(from), Model.Place(to), mode, code)
        }
        Model(legs ?: emptyList())
    } ?: emptyList()
