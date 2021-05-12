package me.cpele.androcommut.tripselection

import android.util.Log
import me.cpele.afk.Outcome
import me.cpele.afk.parseDateTime
import me.cpele.androcommut.NavitiaJourney
import me.cpele.androcommut.NavitiaJourneysResult
import me.cpele.androcommut.NavitiaSection
import me.cpele.androcommut.core.Journey
import me.cpele.androcommut.core.Place
import me.cpele.androcommut.core.Section
import java.util.*

data class TripSelectionModel(
    val journeys: List<Journey>,
    val errors: List<Exception>
)

fun model(outcome: Outcome<NavitiaJourneysResult>): TripSelectionModel =
    when (outcome) {
        is Outcome.Success -> TripSelectionModel(
            journeys = journeys(outcome.value),
            errors = emptyList()
        )
        is Outcome.Failure -> TripSelectionModel(
            journeys = emptyList(),
            errors = listOf(Exception("Journey request failed", outcome.error))
        )
    }

private fun journeys(navitiaJourneysResult: NavitiaJourneysResult): List<Journey> =
    navitiaJourneysResult.journeys
        ?.map { remoteJourney -> journey(remoteJourney) }
        .also { Log.d(navitiaJourneysResult.javaClass.simpleName, "Models: $it") }
        ?: emptyList()

private fun journey(remoteJourney: NavitiaJourney): Journey {
    val remoteSections = remoteJourney.sections
    val sections = remoteSections
        ?.map { remoteSection -> section(remoteSection) }
    return Journey(sections ?: emptyList())
}

private fun section(remoteSection: NavitiaSection): Section {
    val remoteDuration = remoteSection.duration
    val duration = remoteDuration
        ?: throw IllegalStateException("Duration should not be null for $remoteSection")
    val from = remoteSection.from?.name ?: "Unknown origin"
    val to = remoteSection.to?.name ?: "Unknown destination"
    val originPlace = Place(from)
    val destinationPlace = Place(to)
    return when (remoteSection.type) {
        "transfer" -> {
            transfer(remoteSection, duration, originPlace, destinationPlace)
        }
        "waiting" -> {
            wait(remoteSection, duration)
        }
        "street_network", "crow_fly" -> {
            access(remoteSection, duration, originPlace, destinationPlace)
        }
        "public_transport" -> {
            publicTransport(remoteSection, duration, originPlace, destinationPlace)
        }
        else -> throw IllegalStateException("Unknown section type ${remoteSection.type} for $remoteSection")
    }
}

fun wait(remoteSection: NavitiaSection, duration: Long): Section {
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Wait(duration, startTime)
}

private fun publicTransport(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.PublicTransport {
    val mode = remoteSection.display_informations?.commercial_mode ?: "?"
    val code = remoteSection.display_informations?.code ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Move.PublicTransport(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode,
        code
    )
}

fun parse(dateTimeStr: String?): Date = parseDateTime(dateTimeStr) ?: Date()

private fun access(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.Access {
    val mode = remoteSection.mode ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Move.Access(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode
    )
}

private fun transfer(
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.Transfer {
    val mode = remoteSection.transfer_type ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Move.Transfer(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode
    )
}
