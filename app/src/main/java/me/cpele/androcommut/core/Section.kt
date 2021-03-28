package me.cpele.androcommut.core

import java.util.*

sealed class Section {

    abstract val startTime: Date
    abstract val durationSec: Long

    sealed class Move : Section() {
        abstract val origin: Place
        abstract val destination: Place
        abstract val mode: String
        open val summary: CharSequence get() = mode.capitalize(Locale.getDefault())

        data class PublicTransport(
            override val startTime: Date,
            override val durationSec: Long,
            override val origin: Place,
            override val destination: Place,
            override val mode: String,
            val line: String,
            override val summary: CharSequence = "$mode $line"
        ) : Move()

        data class Access(
            override val startTime: Date,
            override val durationSec: Long,
            override val origin: Place,
            override val destination: Place,
            override val mode: String
        ) : Move()

        data class Transfer(
            override val startTime: Date,
            override val durationSec: Long,
            override val origin: Place,
            override val destination: Place,
            override val mode: String
        ) : Move()
    }

    data class Wait(
        override val durationSec: Long,
        override val startTime: Date
    ) : Section()
}