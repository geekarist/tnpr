package me.cpele.androcommut

class Event<T>(private val value: T) {

    private var isConsumed = false

    fun consume(block: (T) -> Unit) = synchronized(this) {
        if (!isConsumed) {
            isConsumed = true
            block(value)
        }
    }
}
