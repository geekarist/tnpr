package me.cpele.androcommut.origdest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.cpele.androcommut.Event

class OriginDestinationViewModel : ViewModel() {

    private val _stateLive = MutableLiveData<State>()
    val stateLive: LiveData<State> get() = _stateLive

    private val _effectLive = MutableLiveData<Event<Effect>>()
    val effectLive: LiveData<Event<Effect>> get() = _effectLive

    sealed class Effect {
        sealed class NavigateToAutosuggest : Effect() {
            object Origin : NavigateToAutosuggest()
            object Destination : NavigateToAutosuggest()
        }
    }

    data class State(val origin: String? = null, val destination: String? = null)

    fun dispatch(intention: Intention) = viewModelScope.launch {
        val effect = when (intention) {
            is Intention.OriginClicked -> Effect.NavigateToAutosuggest.Origin
            is Intention.DestinationClicked -> Effect.NavigateToAutosuggest.Destination
        }
        val event: Event<Effect> = Event(effect)
        withContext(Dispatchers.Main) { _effectLive.value = event }
    }

    sealed class Intention {
        object OriginClicked : Intention()
        object DestinationClicked : Intention()
    }
}