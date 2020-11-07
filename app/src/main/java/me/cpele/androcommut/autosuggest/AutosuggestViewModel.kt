package me.cpele.androcommut.autosuggest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
class AutosuggestViewModel : ViewModel() {

    private val queryFlow = MutableStateFlow<String?>(null)

    private val debouncedQueryFlow = queryFlow.debounce(500)

    init {
        debouncedQueryFlow.onEach { query ->
            Log.d(javaClass.simpleName, "To do: search for $query")
        }.launchIn(viewModelScope)
    }

    fun dispatch(intention: Intention) {
        when (intention) {
            is Intention.QueryEdited -> queryFlow.value = intention.text.toString()
        }
    }

    sealed class Intention {
        data class QueryEdited(val text: CharSequence?) : Intention()
    }
}