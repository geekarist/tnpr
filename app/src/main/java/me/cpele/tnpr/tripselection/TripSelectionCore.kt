package me.cpele.tnpr.tripselection

import me.cpele.afk.Outcome
import me.cpele.afk.parseDateTime
import me.cpele.tnpr.NavitiaJourney
import me.cpele.tnpr.NavitiaJourneysResult
import me.cpele.tnpr.NavitiaSection
import me.cpele.tnpr.core.Journey
import me.cpele.tnpr.core.Place
import me.cpele.tnpr.core.Section
import java.util.*

data class TripSelectionModel(
    val journeys: List<Journey>,
    val errors: List<Throwable>
)

fun model(
    translator: TripSelectionTranslator,
    outcome: Outcome<NavitiaJourneysResult>
): TripSelectionModel =
    when (outcome) {
        is Outcome.Success -> {
            val result = outcome.value
            successToModel(translator, result)
        }
        is Outcome.Failure -> TripSelectionModel(
            journeys = emptyList(),
            errors = listOf(Exception("Journey request failed", outcome.error))
        )
    }

private fun successToModel(
    translator: TripSelectionTranslator,
    result: NavitiaJourneysResult
): TripSelectionModel {

    val outcomes: List<Outcome<Journey>> = result.journeys
        ?.map { remoteJourney ->
            try {
                Outcome.Success(journey(translator, remoteJourney))
            } catch (e: Exception) {
                Outcome.Failure(e)
            }
        }
        ?: emptyList()

    val (journeyOutcomes, errorOutcomes) = outcomes.partition { it is Outcome.Success }
    val journeys = journeyOutcomes.map { it as Outcome.Success }.map { it.value }
    val errors = errorOutcomes.map { it as Outcome.Failure }.map { it.error }

    return TripSelectionModel(
        journeys = journeys,
        errors = errors
    )
}

private fun journey(translator: TripSelectionTranslator, remoteJourney: NavitiaJourney): Journey {
    val remoteSections = remoteJourney.sections
    val sections = if (remoteSections.isNullOrEmpty()) {
        emptyList()
    } else {
        remoteSections
            .plus(remoteSections.last())
            .zipWithNext()
            .map { (remoteSection, nextRemoteSection) ->
                section(
                    translator,
                    remoteSection,
                    nextRemoteSection
                )
            }
    }
    return Journey(sections)
}

private fun section(
    translator: TripSelectionTranslator,
    remoteSection: NavitiaSection,
    nextRemoteSection: NavitiaSection
): Section {
    val remoteDuration = remoteSection.duration
    val duration = remoteDuration
        ?: throw IllegalStateException("Duration should not be null for $remoteSection")
    val from = remoteSection.from?.name ?: "Unknown origin"
    val to = remoteSection.to?.name ?: "Unknown destination"
    val originPlace = Place(from)
    val destinationPlace = Place(to)
    return when (remoteSection.type) {
        "transfer" -> {
            transfer(translator, remoteSection, duration, originPlace, destinationPlace)
        }
        "waiting" -> {
            wait(remoteSection, nextRemoteSection, duration)
        }
        "street_network", "crow_fly" -> {
            access(translator, remoteSection, duration, originPlace, destinationPlace)
        }
        "public_transport" -> {
            publicTransport(remoteSection, duration, originPlace, destinationPlace)
        }
        else -> throw IllegalStateException("Unknown section type ${remoteSection.type} for $remoteSection")
    }
}

fun wait(
    remoteSection: NavitiaSection,
    nextRemoteSection: NavitiaSection,
    duration: Long
): Section {
    val startTime: Date = parse(remoteSection.departure_date_time)
    val place = nextRemoteSection.from?.name
    return Section.Wait(duration, startTime, place ?: "?")
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
    translator: TripSelectionTranslator,
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.Access {
    val mode = translator.processMode(remoteSection.mode) ?: "?"
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
    translator: TripSelectionTranslator,
    remoteSection: NavitiaSection,
    durationMs: Long,
    originPlace: Place,
    destinationPlace: Place
): Section.Move.Transfer {
    val mode = translator.processTransferType(remoteSection.transfer_type) ?: "?"
    val startTime: Date = parse(remoteSection.departure_date_time)
    return Section.Move.Transfer(
        startTime,
        durationMs,
        originPlace,
        destinationPlace,
        mode
    )
}
