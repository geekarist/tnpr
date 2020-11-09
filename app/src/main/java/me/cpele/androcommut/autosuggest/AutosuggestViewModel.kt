package me.cpele.androcommut.autosuggest

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.androcommut.NavitiaService
import me.cpele.androcommut.R
import me.cpele.androcommut.autosuggest.AutosuggestViewModel.*

@FlowPreview
class AutosuggestViewModel(
    private val navitiaService: NavitiaService,
    private val application: Application
) : ViewModel(), Model<Intention, State, Effect> {

    private val _stateLive = MutableLiveData<State>().apply { value = State(emptyList()) }
    override val stateLive: LiveData<State>
        get() = _stateLive

    override val effectLive: LiveData<Event<Effect>>
        get() = TODO("Not yet implemented")

    private val queryFlow = MutableStateFlow<String?>(null)

    init {
        queryFlow.debounce(500)
            .flowOn(Dispatchers.Default)
            .map { query -> navitiaService.places(q = query) }
            .flowOn(Dispatchers.IO)
            .map { navitiaPlaces ->
                navitiaPlaces.places.map { navitiaPlace ->
                    PlaceUiModel(
                        label = navitiaPlace.label
                            ?: navitiaPlace.name
                            ?: navitiaPlace.id
                            ?: application.getString(R.string.autosuggest_unknown_place)
                    )
                }
            }
            .flowOn(Dispatchers.Default)
            .onEach { placeUiModels ->
                val currentState = stateLive.value
                val newState = currentState?.copy(places = placeUiModels)
                _stateLive.value = newState
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    override fun dispatch(intention: Intention) {
        when (intention) {
            is Intention.QueryEdited -> queryFlow.value = intention.text.toString()
        }
    }

    sealed class Intention {
        data class QueryEdited(val text: CharSequence?) : Intention()
    }

    data class State(val places: List<PlaceUiModel>)

    sealed class Effect
}