package me.cpele.androcommut.core

/**
 *  A leg can be:
 *  - An access leg (first or last leg eg walking to get to a scheduled stop point)
 *  - A ride leg (between 2 scheduled stop points) in a public transport vehicle
 *  - A connection leg (between two stop points but not in a public transport vehicle)
 *  - An other leg (not an access, ride or connection leg)
 *  Any leg has a mode, a ride leg has a line
 */
sealed class Leg {

    abstract val duration: String
    abstract val origin: Place
    abstract val destination: Place
    abstract val mode: String

    data class Ride(
        override val duration: String,
        override val origin: Place,
        override val destination: Place,
        override val mode: String,
        val line: String
    ) : Leg()

    data class Access(
        override val duration: String,
        override val origin: Place,
        override val destination: Place,
        override val mode: String
    ) : Leg()

    data class Connection(
        override val duration: String,
        override val origin: Place,
        override val destination: Place,
        override val mode: String
    ) : Leg()
}