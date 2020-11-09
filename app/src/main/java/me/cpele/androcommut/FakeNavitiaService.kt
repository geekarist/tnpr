package me.cpele.androcommut

class FakeNavitiaService : NavitiaService {
    override fun places(q: String?) = NavitiaPlacesResult(
        listOf(
            NavitiaPlace("place-id-1", "place-name-1", "place-label-1"),
            NavitiaPlace("place-id-2", "place-name-2", "place-label-2"),
            NavitiaPlace("place-id-3", "place-name-3", "place-label-3"),
        )
    )
}
