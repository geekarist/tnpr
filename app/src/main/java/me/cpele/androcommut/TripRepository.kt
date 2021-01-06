package me.cpele.androcommut

import androidx.collection.LruCache
import me.cpele.androcommut.core.Trip

class TripRepository(private val cache: LruCache<String, Trip>) {

    fun put(tripId: String, trip: Trip) {
        cache.put(tripId, trip)
    }
}
