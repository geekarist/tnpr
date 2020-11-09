package me.cpele.androcommut

interface NavitiaService {
    fun places(q: String?): NavitiaPlacesResult
}

data class NavitiaPlacesResult(val places: List<NavitiaPlace>)

data class NavitiaPlace(val id: String?, val name: String?, val label: String?)