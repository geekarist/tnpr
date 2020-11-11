package me.cpele.afk

sealed class Outcome<T>(private val value: T?) {

    data class Success<T>(val value: T) : Outcome<T>(value)
    data class Failure<T>(val error: Throwable) : Outcome<T>(null)
}
