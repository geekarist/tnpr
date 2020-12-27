package me.cpele.androcommut.roadmap

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

class RoadmapViewModel : ViewModel() {
    // TODO: Implement the ViewModel
}

private sealed class Coeffect {
    data class Db(val todo: Nothing = TODO()) : Coeffect()
    data class Http(val todo: Nothing = TODO()) : Coeffect()
}

private sealed class Effect {
    data class Db(val todo: Nothing = TODO()) : Effect()
    data class Http(val todo: Nothing = TODO()) : Effect()
}

private fun process(coeffectFlow: Flow<Coeffect>): Flow<Effect> = TODO()