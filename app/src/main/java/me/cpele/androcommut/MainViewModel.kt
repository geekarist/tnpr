package me.cpele.androcommut

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.androcommut.autosuggest.AutosuggestTrigger

class MainViewModel : ViewModel(), Model<MainViewModel.Intention, Nothing, MainViewModel.Effect> {

    sealed class Intention {
        data class Suggestion(
            val fragmentId: Int,
            val trigger: AutosuggestTrigger?,
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

    override fun dispatch(intention: Intention) {
        TODO("Not yet implemented")
        // fragment.findNavController().navigate(
        //     AutosuggestFragmentDirections.actionAutosuggestFragmentToOriginDestinationFragment(
        //         label.takeIf { trigger == AutosuggestFragment.Trigger.ORIGIN },
        //         label.takeIf { trigger == AutosuggestFragment.Trigger.DESTINATION }
        //     )
        // )
    }

    override val stateLive: LiveData<Nothing>
        get() = TODO("Not yet implemented")
    override val effectLive: LiveData<Event<Effect>>
        get() = TODO("Not yet implemented")
}
