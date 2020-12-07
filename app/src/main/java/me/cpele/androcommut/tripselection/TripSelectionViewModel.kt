package me.cpele.androcommut.tripselection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.afk.exhaust
import me.cpele.androcommut.tripselection.TripSelectionViewModel.*

class TripSelectionViewModel :
    ViewModel(),
    Model<Intention, State, Effect> {

    override fun dispatch(intention: Intention) {
        when (intention) { // TODO: Don't over engineer: `dispatch()`, `Intention`, `State` and `Effect` are enough! Pure functions or middlewares are too much.
            is Intention.Load -> Log.d(
                javaClass.simpleName,
                "Origin is ${intention.originId}: ${intention.originLabel}, " +
                        "destination is: ${intention.destinationId}: ${intention.destinationLabel}"
            )
        }.exhaust()
    }

    override val stateLive: LiveData<State>
        get() = TODO("Not yet implemented")

    override val effectLive: LiveData<Event<Effect>>
        get() = TODO("Not yet implemented")

    sealed class Intention {
        data class Load(
            val originId: String,
            val originLabel: String,
            val destinationId: String,
            val destinationLabel: String
        ) : Intention()
    }

    class State {

    }

    sealed class Effect {
    }
}
