package me.cpele.androcommut.core

import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

// TODO: Move to a core module that has no dependency
data class Trip(
    val legs: List<Leg>
) {

    val originName: CharSequence = legs.filterIsInstance<Leg.Ride>().first().origin.name
    val destinationName: CharSequence = legs.filterIsInstance<Leg.Ride>().last().destination.name

    val legsSummary: CharSequence =
        legs.filterIsInstance<Leg.Ride>()
            .joinToString(", ") { "${it.mode} ${it.line}" }

    private val durationSec: Int = legs.sumBy { it.durationSec.toInt() }

    @ExperimentalTime
    val formattedDuration: String =
        "${durationSec.seconds.inMinutes.roundToInt()} min"
}