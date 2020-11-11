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
import me.cpele.afk.Outcome
import me.cpele.androcommut.BuildConfig
import me.cpele.androcommut.NavitiaPlacesResult
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
        queryFlow.debounce(1000)
            .flowOn(Dispatchers.Default)
            .map { query -> fetchPlaces(query) }
            .flowOn(Dispatchers.IO)
            .map { result -> mapResultToUiModels(result) }
            .flowOn(Dispatchers.Default)
            .onEach { placeUiModels ->
                val currentState = stateLive.value
                val newState = currentState?.copy(places = placeUiModels)
                _stateLive.value = newState
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    private suspend fun fetchPlaces(query: String?): Outcome<NavitiaPlacesResult> =
        try {
            val response = navitiaService.places(auth = BuildConfig.NAVITIA_API_KEY, q = query)
            Outcome.Success(response)
        } catch (t: Throwable) {
            Outcome.Failure(t)
        }

    private fun mapResultToUiModels(result: Outcome<NavitiaPlacesResult>): List<PlaceUiModel> =
        when (result) {
            is Outcome.Success -> result.value.places.map { navitiaPlace ->
                PlaceUiModel(
                    label = navitiaPlace.label
                        ?: navitiaPlace.name
                        ?: navitiaPlace.id
                        ?: application.getString(R.string.autosuggest_unknown_place)
                )
            }
            is Outcome.Failure -> emptyList()
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