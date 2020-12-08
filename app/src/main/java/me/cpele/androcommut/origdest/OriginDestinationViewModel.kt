package me.cpele.androcommut.origdest

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
import me.cpele.androcommut.R
import me.cpele.androcommut.origdest.OriginDestinationViewModel.*

class OriginDestinationViewModel(private val app: Application) : ViewModel(),
    Component<Intention, State, Effect> {

    private val _stateLive =
        MutableLiveData(
            State(
                originId = null,
                originLabel = null,
                destinationId = null,
                destinationLabel = null,
                instructions = app.getString(R.string.od_default_instructions)
            )
        )
    override val stateLive: LiveData<State> get() = _stateLive

    private val _effectLive = MutableLiveData<Event<Effect>>()
    override val effectLive: LiveData<Event<Effect>> get() = _effectLive

    override fun dispatch(intention: Intention) {

        viewModelScope.launch {

            val state = stateLive.value

            val (effect, newState) = when (intention) {
                is Intention.Load -> null to state?.process(intention)
                is Intention.OriginClicked -> Effect.NavigateToAutosuggest.Origin to state
                is Intention.DestinationClicked -> Effect.NavigateToAutosuggest.Destination to state
                is Intention.ActionClicked -> Effect.NavigateToTrip(
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

            withContext(Dispatchers.Main) {
                if (effect != null) _effectLive.value = Event(effect)
                _stateLive.value = newState
            }
        }
    }

    private fun State.process(
        intention: Intention.Load
    ): State = copy(
        originId = intention.originId ?: originId,
        originLabel = intention.originLabel ?: originLabel,
        destinationId = intention.destinationId ?: destinationId,
        destinationLabel = intention.destinationLabel ?: destinationLabel,
        instructions = when {
            intention.originLabel == null && intention.destinationLabel == null -> app.getString(R.string.od_default_instructions)
            intention.originLabel == null -> app.getString(R.string.od_origin_instructions)
            intention.destinationLabel == null -> app.getString(R.string.od_destination_instructions)
            else -> app.getString(R.string.od_ready_instructions)
        }
    )

    sealed class Intention {

        data class Load(
            val originId: String?,
            val originLabel: String?,
            val destinationId: String?,
            val destinationLabel: String?
        ) : Intention()

        object OriginClicked : Intention()
        object DestinationClicked : Intention()
        object ActionClicked : Intention()
    }

    data class State(
        val originId: String?,
        val originLabel: String?,
        val destinationId: String?,
        val destinationLabel: String?,
        val instructions: CharSequence
    )

    sealed class Effect {

        sealed class NavigateToAutosuggest : Effect() {
            object Origin : NavigateToAutosuggest()
            object Destination : NavigateToAutosuggest()
        }

        data class NavigateToTrip(
            val originId: String,
            val originLabel: String,
            val destinationId: String,
            val destinationLabel: String
        ) : Effect()
    }
}