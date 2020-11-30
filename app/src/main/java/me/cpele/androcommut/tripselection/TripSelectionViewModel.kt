package me.cpele.androcommut.tripselection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.cpele.afk.Event
import me.cpele.afk.Model
import me.cpele.afk.exhaust
import me.cpele.androcommut.NavitiaService
import me.cpele.androcommut.tripselection.TripSelectionViewModel.*

class TripSelectionViewModel(navitiaService: NavitiaService) :
    ViewModel(),
    Model<Intention, State, Effect> {

    override fun dispatch(intention: Intention) {
        when (intention) {
            is Intention.Load -> Log.d(
                javaClass.simpleName,
                "Origin: ${intention.origin}, destination: ${intention.destination}"
            )
        }.exhaust()
    }

    override val stateLive: LiveData<State>
        get() = TODO("Not yet implemented")

    override val effectLive: LiveData<Event<Effect>>
        get() = TODO("Not yet implemented")

    sealed class Intention {
        data class Load(val origin: String, val destination: String) : Intention()
    }

    class State {

    }

    sealed class Effect {
    }
}