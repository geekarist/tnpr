package me.cpele.androcommut.core

data class Leg(
    val duration: String, // TODO: Represent Strings as values that can be processed
    val origin: Place,
    val destination: Place,
    val mode: String,
    val line: String
)