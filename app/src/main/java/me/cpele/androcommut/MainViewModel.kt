package me.cpele.androcommut

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.androcommut.MainViewModel.*
import me.cpele.androcommut.autosuggest.AutosuggestTrigger

class MainViewModel : ViewModel(), Model<Intention, State, Effect> {

    private val _stateLive = MutableLiveData<State>()
    override val stateLive: LiveData<State>
        get() = TODO("Not yet implemented")

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val effectLive: LiveData<Event<Effect>>
        get() = _effectLive

    override fun dispatch(intention: Intention) {

        val state = _stateLive.value

        val effect = when (intention) {
            is Intention.Suggestion ->
                when (intention.trigger) {
                    AutosuggestTrigger.ORIGIN -> Effect.SuggestionIdentified(
                        intention.fragmentId,
                        intention.label,
                        state?.destinationLabel
                    )
                    AutosuggestTrigger.DESTINATION -> Effect.SuggestionIdentified(
                        intention.fragmentId,
                        state?.originLabel,
                        intention.label
                    )
                }
        }

        val newState = state?.copy(
            originLabel = effect.originLabel,
            destinationLabel = effect.destinationLabel
        )

        _effectLive.value = Event(effect)
        _stateLive.value = newState
    }

    sealed class Intention {
        data class Suggestion(
            val fragmentId: Int,
            val trigger: AutosuggestTrigger,
            val label: String
        ) : Intention()
    }

    sealed class Effect {
        data class SuggestionIdentified(
            val fragmentId: Int,
            val originLabel: String?,
            val destinationLabel: String?
        ) : Effect()

    }

    data class State(val originLabel: String?, val destinationLabel: String?)
}
