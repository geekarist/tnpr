package me.cpele.androcommut.core

import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

// TODO: Move to a core module that has no dependency
data class Journey(
    val sections: List<Section>
) {

    val originName: CharSequence =
        sections.filterIsInstance<Section.Move.PublicTransport>().first().origin.name
    val destinationName: CharSequence =
        sections.filterIsInstance<Section.Move.PublicTransport>().last().destination.name

    val sectionsSummary: CharSequence =
        sections.filterIsInstance<Section.Move.PublicTransport>()
            .joinToString(", ") { "${it.mode} ${it.line}" }

    private val durationSec: Int = sections.sumBy { it.durationSec.toInt() }

    @ExperimentalTime
    val formattedDuration: String =
        "${durationSec.seconds.inMinutes.roundToInt()} min"
}