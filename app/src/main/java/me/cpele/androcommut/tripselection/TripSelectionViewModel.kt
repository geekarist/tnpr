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

        val navitiaResponse = withContext(Dispatchers.IO) {
            navitiaService.journeys(
                BuildConfig.NAVITIA_API_KEY,
                intention.originId,
                intention.destinationId
            )
        }

        val state = stateLive.value

        val model = state?.copy(uiModels = navitiaResponse.toUiModels())

        withContext(Dispatchers.Main) {
            _stateLive.value = model
        }
    }

    private fun NavitiaJourneysResult.toUiModels(): List<UiModel> =
        journeys?.map { remoteJourney ->
            val remoteSections = remoteJourney.sections
            val legs = remoteSections?.map { remoteSection ->
                val remoteDuration = remoteSection.duration
                val duration = remoteDuration?.toString()
                    ?: "Unknown duration" // TODO: Extract string resources
                val from = remoteSection.from?.name ?: "Unknown origin"
                val to = remoteSection.to?.name ?: "Unknown destination"
                UiModel.Leg(duration, UiModel.Place(from), UiModel.Place(to))
            }
            UiModel(legs ?: emptyList())
        } ?: emptyList()

    sealed class Intention {
        data class Load(
            val originId: String,
            val originLabel: String,
            val destinationId: String,
            val destinationLabel: String
        ) : Intention()
    }

    data class State(
        val uiModels: List<UiModel>? = null
    )

    sealed class Consequence {
    }

    data class UiModel(
        val legs: List<Leg>
    ) {
        data class Leg(val duration: String, val origin: Place, val destination: Place)
        data class Place(val name: String)
    }
}
