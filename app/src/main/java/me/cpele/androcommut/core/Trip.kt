package me.cpele.androcommut.core

// TODO: Move to a core module that has no dependency
data class Trip(
    val legs: List<Leg>
) {

    val legsSummary: CharSequence =
        legs.filterIsInstance<Leg.Ride>()
            .joinToString(", ") { "${it.mode} ${it.line}" }

    val duration: Int = legs.sumBy { it.duration.toInt() }
}