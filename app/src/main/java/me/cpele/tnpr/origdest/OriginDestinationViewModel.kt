package me.cpele.tnpr.origdest

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.cpele.afk.Component
import me.cpele.afk.Event
import me.cpele.tnpr.R
import me.cpele.tnpr.origdest.OriginDestinationViewModel.*

class OriginDestinationViewModel(private val app: Application) : ViewModel(),
    Component<Action, State, Effect> {

    private val _stateLive =
        MutableLiveData(
            State(
                originId = null,
                originLabel = null,
                destinationId = null,
                destinationLabel = null,
                instructions = app.getString(R.string.od_default_instructions),
                isActionAllowed = false
            )
        )
    override val stateLive: LiveData<State> get() = _stateLive

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val eventLive: LiveData<Event<Effect>> get() = _effectLive

    override fun dispatch(action: Action) {

        viewModelScope.launch {

            val state = stateLive.value

            val (effect, newState) = withContext(Dispatchers.Default) {
                when (action) {
                    is Action.Load -> null to state?.process(action)
                    is Action.OriginClicked -> Effect.NavigateToAutosuggest.Origin(
                        state?.originId,
                        state?.originLabel
                    ) to state
                    is Action.DestinationClicked -> Effect.NavigateToAutosuggest.Destination(
                        state?.destinationId,
                        state?.destinationLabel
                    ) to state
                    is Action.ActionClicked -> Effect.NavigateToTripSelection(
                        originId = state?.originId
                            ?: throw IllegalStateException("State is missing an origin ID: $state"),
                        originLabel = state.originLabel
                            ?: throw IllegalStateException("State is missing an origin label: $state"),
                        destinationId = state.destinationId
                            ?: throw IllegalStateException("State is missing a destination ID: $state"),
                        destinationLabel = state.destinationLabel
                            ?: throw IllegalStateException("State is missing a destination label: $state")
                    ) to state
                }
            }

            withContext(Dispatchers.Main) {
                if (effect != null) _effectLive.value = Event(effect)
                _stateLive.value = newState
            }
        }
    }

    private fun State.process(
        action: Action.Load
    ): State = copy(
        originId = action.originId ?: originId,
        originLabel = action.originLabel ?: originLabel,
        destinationId = action.destinationId ?: destinationId,
        destinationLabel = action.destinationLabel ?: destinationLabel,
        instructions = when {
            action.originLabel == null && action.destinationLabel == null -> app.getString(R.string.od_default_instructions)
            action.originLabel == null -> app.getString(R.string.od_origin_instructions)
            action.destinationLabel == null -> app.getString(R.string.od_destination_instructions)
            else -> app.getString(R.string.od_ready_instructions)
        },
        isActionAllowed = when {
            action.originId == null || action.originLabel == null -> false
            action.destinationId == null || action.destinationLabel == null -> false
            else -> true
        }
    )

    sealed class Action {

        data class Load(
            val originId: String?,
            val originLabel: String?,
            val destinationId: String?,
            val destinationLabel: String?
        ) : Action()

        object OriginClicked : Action()
        object DestinationClicked : Action()
        object ActionClicked : Action()
    }

    data class State(
        val originId: String?,
        val originLabel: String?,
        val destinationId: String?,
        val destinationLabel: String?,
        val instructions: CharSequence,
        val isActionAllowed: Boolean
    )

    sealed class Effect {

        sealed class NavigateToAutosuggest : Effect() {
            data class Origin(val id: String?, val label: String?) : NavigateToAutosuggest()
            data class Destination(val id: String?, val label: String?) : NavigateToAutosuggest()
        }

        data class NavigateToTripSelection(
            val originId: String,
            val originLabel: String,
            val destinationId: String,
            val destinationLabel: String
        ) : Effect()
    }
}