package me.cpele.androcommut

class Event<T>(private val value: T) {

    private var isConsumed = false

    fun consume(): T = synchronized(this) {
        isConsumed = true
        value
    }
}
