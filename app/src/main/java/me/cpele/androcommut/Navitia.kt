package me.cpele.androcommut

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NavitiaService {

    @GET("/v1/coverage/fr-idf/places")
    suspend fun places(
        @Header("Authorization") auth: String,
        @Query("q") q: String
    ): NavitiaPlacesResult

    @GET("/v1/coverage/fr-idf/journeys")
    suspend fun journeys(
        @Header("Authorization") auth: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): NavitiaJourneysResult
}

data class NavitiaJourneysResult(val journeys: List<NavitiaJourney>?)

data class NavitiaJourney(val sections: List<NavitiaSection>?)

data class NavitiaSection(val duration: Int?, val from: NavitiaPlace?, val to: NavitiaPlace?)

data class NavitiaPlacesResult(val places: List<NavitiaPlace>) // TODO: Make nullable

data class NavitiaPlace(val id: String?, val name: String?)