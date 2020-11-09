package me.cpele.afk

class Event<T>(private val value: T) {

    private var isConsumed = false

    fun consume(block: (T) -> Unit) = synchronized(this) {
        if (!isConsumed) {
            isConsumed = true
            block(value)
        }
    }
}
