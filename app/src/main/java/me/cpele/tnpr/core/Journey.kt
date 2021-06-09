package me.cpele.tnpr.core

import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

// TODO: Move to a core module that has no dependency
data class Journey(
    val sections: List<Section>
) {

    val originName: CharSequence =
        sections.filterIsInstance<Section.Move.PublicTransport>().firstOrNull()?.origin?.name
            ?: sections.filterIsInstance<Section.Move>().firstOrNull()?.origin?.name
            ?: throw IllegalStateException("Unknown origin")

    val destinationName: CharSequence =
        sections.filterIsInstance<Section.Move.PublicTransport>().lastOrNull()?.destination?.name
            ?: sections.filterIsInstance<Section.Move>().lastOrNull()?.destination?.name
            ?: throw IllegalStateException("Unknown destination")

    val sectionsSummary: CharSequence =
        sections.filterIsInstance<Section.Move>()
            .let {
                if (it.size > 1) {
                    it.filterIsInstance<Section.Move.PublicTransport>()
                } else {
                    it
                }
            }
            .joinToString(", ") { it.summary }

    private val durationSec: Int = sections.sumBy { it.durationSec.toInt() }

    @ExperimentalTime
    val formattedDuration: String =
        "${durationSec.seconds.inMinutes.roundToInt()} min"
}