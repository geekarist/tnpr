package me.cpele.androcommut

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.androcommut.autosuggest.AutosuggestTrigger

class MainViewModel : ViewModel(), Model<MainViewModel.Intention, Nothing, MainViewModel.Effect> {

    override val stateLive: LiveData<Nothing>
        get() = TODO("Not yet implemented")

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val effectLive: LiveData<Event<Effect>>
        get() = _effectLive

    override fun dispatch(intention: Intention) {

        val effect = when (intention) {
            is Intention.Suggestion ->
                when (intention.trigger) {
                    AutosuggestTrigger.ORIGIN -> Effect.SuggestionIdentified(
                        intention.fragmentId,
                        intention.label,
                        null
                    )
                    AutosuggestTrigger.DESTINATION -> Effect.SuggestionIdentified(
                        intention.fragmentId,
                        null,
                        intention.label
                    )
                }
        }

        _effectLive.value = Event(effect)
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
}
