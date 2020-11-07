package me.cpele.androcommut.origdest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.cpele.androcommut.Event
import me.cpele.androcommut.Model
import me.cpele.androcommut.origdest.OriginDestinationViewModel.*

class OriginDestinationViewModel : ViewModel(), Model<Intention, State, Effect> {

    private val _stateLive = MutableLiveData<State>()
    override val stateLive: LiveData<State> get() = _stateLive

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val effectLive: LiveData<Event<Effect>> get() = _effectLive

    override fun dispatch(intention: Intention) {
        viewModelScope.launch {
            val effect = when (intention) {
                is Intention.OriginClicked -> Effect.NavigateToAutosuggest.Origin
                is Intention.DestinationClicked -> Effect.NavigateToAutosuggest.Destination
            }
            val event: Event<Effect> = Event(effect)
            withContext(Dispatchers.Main) { _effectLive.value = event }
        }
    }

    sealed class Intention {
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