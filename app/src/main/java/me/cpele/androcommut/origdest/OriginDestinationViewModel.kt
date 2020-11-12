package me.cpele.androcommut.origdest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.androcommut.origdest.OriginDestinationViewModel.*

class OriginDestinationViewModel : ViewModel(), Model<Intention, State, Effect> {

    private val _stateLive = MutableLiveData(State(null, null))
    override val stateLive: LiveData<State> get() = _stateLive

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val effectLive: LiveData<Event<Effect>> get() = _effectLive

    override fun dispatch(intention: Intention) {
        viewModelScope.launch {
            val (effect, newState) = when (intention) {
                is Intention.Load -> null to stateLive.value?.copy(
                    origin = intention.origin,
                    destination = intention.destination
                )
                is Intention.OriginClicked -> Effect.NavigateToAutosuggest.Origin to stateLive.value
                is Intention.DestinationClicked -> Effect.NavigateToAutosuggest.Destination to stateLive.value
            }
            withContext(Dispatchers.Main) {
                if (effect != null) _effectLive.value = Event(effect)
                _stateLive.value = newState
            }
        }
    }

    sealed class Intention {

        data class Load(val origin: String?, val destination: String?) : Intention()

        object OriginClicked : Intention()
        object DestinationClicked : Intention()
    }

    data class State(val origin: String? = null, val destination: String? = null)

    sealed class Effect {

        sealed class NavigateToAutosuggest : Effect() {
            object Origin : NavigateToAutosuggest()
            object Destination : NavigateToAutosuggest()
        }
    }
}