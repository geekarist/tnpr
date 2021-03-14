package me.cpele.androcommut.core

import java.util.*

/**
 *  A leg can be:
 *  - An access leg (first or last leg eg walking to get to a scheduled stop point)
 *  - A ride leg (between 2 scheduled stop points) in a public transport vehicle
 *  - A connection leg (between two stop points but not in a public transport vehicle)
 *  - An other leg (not an access, ride or connection leg)
 *  Any leg has a mode, a ride leg has a line
 */
sealed class Leg {

    abstract val startTime: Date
    abstract val durationSec: Long
    abstract val origin: Place
    abstract val destination: Place
    abstract val mode: String

    data class Ride(
        override val startTime: Date,
        override val durationSec: Long,
        override val origin: Place,
        override val destination: Place,
        override val mode: String,
        val line: String
    ) : Leg()

    data class Access(
        override val startTime: Date,
        override val durationSec: Long,
        override val origin: Place,
        override val destination: Place,
        override val mode: String
    ) : Leg()

    data class Connection(
        override val startTime: Date,
        override val durationSec: Long,
        override val origin: Place,
        override val destination: Place,
        override val mode: String,
        val waitTimeAtEnd: Long?
    ) : Leg()
}