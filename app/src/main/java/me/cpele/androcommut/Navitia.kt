package me.cpele.androcommut

interface NavitiaService {
    fun places(q: String?): NavitiaPlacesResult
}

data class NavitiaPlacesResult(val places: List<NavitiaPlaces>)

data class NavitiaPlaces(val id: String?, val name: String?, val label: String?)