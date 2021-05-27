package me.cpele.androcommut.autosuggest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import me.cpele.afk.Component
import me.cpele.afk.Event
import me.cpele.afk.Outcome
import me.cpele.afk.exhaust
import me.cpele.androcommut.BuildConfig
import me.cpele.androcommut.NavitiaPlacesResult
import me.cpele.androcommut.NavitiaService
import me.cpele.androcommut.autosuggest.AutosuggestViewModel.*
import java.util.*

@FlowPreview
class AutosuggestViewModel(
    private val navitiaService: NavitiaService
) : ViewModel(), Component<Action, State, Effect> {

    private val _stateLive = MutableLiveData<State>().apply {
        value = State(
            answer = SuggestAnswerUiModel.Some(emptyList()),
            isQueryClearable = false,
            isRefreshing = false
        )
    }
    override val stateLive: LiveData<State>
        get() = _stateLive

    override val eventLive: LiveData<Event<Effect>>
        get() = TODO("Not yet implemented")

    private data class Query(val value: String?, val id: UUID = UUID.randomUUID())

    private val queryFlow = MutableStateFlow(Query(null))


    private sealed class InitialOperation {
        data class PostEmptyState(val stateValue: State?) : InitialOperation()
        object Pass : InitialOperation()
    }

    init {
        queryFlow
            // 0. Prepare and filter query
            .debounce(1000)
            .map { it.value }
            .filterNotNull()
            // 1. Initial state and post
            .map { query -> pairQueryToInitialOp(query) }
            .flowOn(Dispatchers.Default)
            .onEach { (_, initialOp) -> maybePostEmptyState(initialOp) }
            .flowOn(Dispatchers.Main)
            // 2. State at start (loading) and post
            .filterNot { (query, _) -> query.isBlank() }
            .map { (query, _) -> pairQueryToStateAtStart(query) }
            .flowOn(Dispatchers.Default)
            .onEach { (_, stateAtStart) -> _stateLive.value = stateAtStart }
            .flowOn(Dispatchers.Main)
            // 3. Fetch suggestions
            .map { (query, _) -> fetchPlaces(query) }
            .flowOn(Dispatchers.IO)
            // 4. Map to UI model then post final state
            .map { result -> result.toUiModel() }
            .map { uiModel -> mapToFinalState(uiModel) }
            .flowOn(Dispatchers.Default)
            .onEach { newState -> _stateLive.value = newState }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    private fun mapToFinalState(uiModel: SuggestAnswerUiModel) =
        stateLive.value?.copy(
            answer = uiModel,
            isRefreshing = false,
            isQueryClearable = true
        )

    private fun pairQueryToStateAtStart(query: String) =
        query to _stateLive.value?.copy(
            isRefreshing = true,
            isQueryClearable = false
        )

    private fun maybePostEmptyState(initialOp: InitialOperation) {
        if (initialOp is InitialOperation.PostEmptyState) {
            _stateLive.value = initialOp.stateValue
        }
    }

    private fun pairQueryToInitialOp(query: String) = query to if (query.isBlank()) {
        InitialOperation.PostEmptyState(
            _stateLive.value?.copy(
                answer = SuggestAnswerUiModel.Some(emptyList()),
                isQueryClearable = false
            )
        )
    } else {
        InitialOperation.Pass
    }

    private suspend fun fetchPlaces(query: String): Outcome<NavitiaPlacesResult> =
        try {
            val response = navitiaService.places(auth = BuildConfig.NAVITIA_API_KEY, q = query)
            Outcome.Success(response)
        } catch (t: Throwable) {
            Outcome.Failure(t)
        }

    private fun Outcome<NavitiaPlacesResult>.toUiModel(): SuggestAnswerUiModel =
        when (this) {
            is Outcome.Success ->
                value.places?.map { navitiaPlace ->
                    PlaceUiModel(
                        id = navitiaPlace.id
                            ?: throw IllegalStateException("Place has no id: $navitiaPlace"),
                        name = navitiaPlace.name
                            ?: throw IllegalStateException("Place has no name: $navitiaPlace"),
                        label = navitiaPlace.name
                    )
                }?.let { placeUiModels ->
                    if (placeUiModels.isEmpty()) {
                        SuggestAnswerUiModel.None
                    } else {
                        SuggestAnswerUiModel.Some(placeUiModels)
                    }
                } ?: "Result should have places: $value".let { msg ->
                    SuggestAnswerUiModel.Fail(msg, IllegalStateException(msg))
                }
            is Outcome.Failure -> SuggestAnswerUiModel.Fail("Error fetching places", error)
        }

    override fun dispatch(action: Action) {
        when (action) {
            is Action.QueryEdited -> {
                val query = action.text
                queryFlow.value = queryFlow.value.copy(
                    value = query?.toString()
                )
            }
            is Action.QueryRetry -> {
                queryFlow.value = queryFlow.value.copy(
                    value = queryFlow.value.toString(),
                    id = UUID.randomUUID()
                )
                _stateLive.value = _stateLive.value?.copy(
                    answer = SuggestAnswerUiModel.Some(
                        emptyList()
                    )
                )
            }
        }.exhaust()
    }

    sealed class Action {

        data class QueryEdited(val text: CharSequence?) : Action()
        object QueryRetry : Action()
    }

    data class State(
        val answer: SuggestAnswerUiModel,
        val isQueryClearable: Boolean,
        val isRefreshing: Boolean
    )

    sealed class Effect
}