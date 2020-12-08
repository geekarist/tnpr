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
) : ViewModel(), Component<Intention, State, Effect> {

    private val _stateLive = MutableLiveData(State())
    override val stateLive: LiveData<State>
        get() = _stateLive

    override val effectLive: LiveData<Event<Effect>>
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

        val model = navitiaResponse.toUiModel()

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
        val tripUiModels: List<TripUiModel>? = null
    )

    sealed class Effect {
    }

    data class TripUiModel(
        val xx: String = TODO()
    )
}

private fun NavitiaJourneysResult.toUiModel(): State? {
    TODO("Not yet implemented")
}
