package me.cpele.androcommut.origdest

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.androcommut.R
import me.cpele.androcommut.origdest.OriginDestinationViewModel.*

class OriginDestinationViewModel(private val app: Application) : ViewModel(),
    Model<Intention, State, Effect> {

    private val _stateLive =
        MutableLiveData(
            State(
                origin = null,
                destination = null,
                instructions = app.getString(R.string.od_default_instructions)
            )
        )
    override val stateLive: LiveData<State> get() = _stateLive

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val effectLive: LiveData<Event<Effect>> get() = _effectLive

    override fun dispatch(intention: Intention) {

        viewModelScope.launch {

            val state = stateLive.value

            val (effect, newState) = when (intention) {
                is Intention.Load -> null to state?.process(intention)
                is Intention.OriginClicked -> Effect.NavigateToAutosuggest.Origin to state
                is Intention.DestinationClicked -> Effect.NavigateToAutosuggest.Destination to state
            }

            withContext(Dispatchers.Main) {
                if (effect != null) _effectLive.value = Event(effect)
                _stateLive.value = newState
            }
        }
    }

    private fun State.process(
        intention: Intention.Load
    ): State? = copy(
        origin = intention.origin ?: origin,
        destination = intention.destination ?: destination,
        instructions = when {
            intention.origin == null && intention.destination == null -> app.getString(R.string.od_default_instructions)
            intention.origin == null -> app.getString(R.string.od_origin_instructions)
            intention.destination == null -> app.getString(R.string.od_destination_instructions)
            else -> app.getString(R.string.od_ready_instructions)
        }
    )

    sealed class Intention {

        data class Load(val origin: String?, val destination: String?) : Intention()

        object OriginClicked : Intention()
        object DestinationClicked : Intention()
    }

    data class State(
        val origin: String?,
        val destination: String?,
        val instructions: CharSequence
    )

    sealed class Effect {

        sealed class NavigateToAutosuggest : Effect() {
            object Origin : NavigateToAutosuggest()
            object Destination : NavigateToAutosuggest()
        }
    }
}