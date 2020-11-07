package me.cpele.androcommut.autosuggest

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.cpele.androcommut.Event
import me.cpele.androcommut.Model
import me.cpele.androcommut.autosuggest.AutosuggestViewModel.*

@FlowPreview
class AutosuggestViewModel : ViewModel(), Model<Intention, State, Effect> {

    override val stateLive: LiveData<State>
        get() = TODO("Not yet implemented")
    override val effectLive: LiveData<Event<Effect>>
        get() = TODO("Not yet implemented")

    private val queryFlow = MutableStateFlow<String?>(null)

    private val debouncedQueryFlow = queryFlow.debounce(500)

    init {
        debouncedQueryFlow.onEach { query ->
            Log.d(javaClass.simpleName, "To do: search for [$query]")
        }.launchIn(viewModelScope)
    }

    override fun dispatch(intention: Intention) {
        when (intention) {
            is Intention.QueryEdited -> queryFlow.value = intention.text.toString()
        }
    }

    sealed class Intention {
        data class QueryEdited(val text: CharSequence?) : Intention()
    }

    sealed class State

    sealed class Effect
}