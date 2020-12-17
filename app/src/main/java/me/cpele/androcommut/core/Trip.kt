package me.cpele.androcommut.core

data class Trip(
    val legs: List<Leg>
) {

    val legsSummary: CharSequence? by lazy {
        legs.joinToString(", ") { "${it.mode} ${it.line}" }
    }

    val duration: Int by lazy {
        legs.sumBy { it.duration.toInt() }
    }

}