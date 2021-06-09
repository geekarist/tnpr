package me.cpele.tnpr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.cpele.afk.Component
import me.cpele.afk.Event
import me.cpele.tnpr.MainViewModel.*
import me.cpele.tnpr.autosuggest.AutosuggestTrigger

class MainViewModel : ViewModel(), Component<Action, State, Effect> {

    private val _stateLive = MutableLiveData(State())
    override val stateLive: LiveData<State>
        get() = TODO("Not yet implemented")

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val eventLive: LiveData<Event<Effect>>
        get() = _effectLive

    override fun dispatch(action: Action) {

        val state = _stateLive.value

        val effect = when (action) {
            is Action.Suggestion ->
                when (action.trigger) {
                    AutosuggestTrigger.ORIGIN -> Effect.SuggestionIdentified(
                        action.fragmentId,
                        action.id,
                        action.label,
                        state?.destinationId,
                        state?.destinationLabel
                    )
                    AutosuggestTrigger.DESTINATION -> Effect.SuggestionIdentified(
                        action.fragmentId,
                        state?.originId,
                        state?.originLabel,
                        action.id,
                        action.label
                    )
                }
        }

        val newState = state?.copy(
            originId = effect.originId,
            originLabel = effect.originLabel,
            destinationId = effect.destinationId,
            destinationLabel = effect.destinationLabel
        )

        _effectLive.value = Event(effect)
        _stateLive.value = newState
    }

    sealed class Action {
        data class Suggestion(
            val fragmentId: Int,
            val trigger: AutosuggestTrigger,
            val id: String,
            val label: String
        ) : Action()
    }

    sealed class Effect {
        data class SuggestionIdentified(
            val fragmentId: Int,
            val originId: String?,
            val originLabel: String?,
            val destinationId: String?,
            val destinationLabel: String?
        ) : Effect()

    }

    data class State(
        val originLabel: String? = null,
        val destinationLabel: String? = null,
        val originId: String? = null,
        val destinationId: String? = null
    )
}
