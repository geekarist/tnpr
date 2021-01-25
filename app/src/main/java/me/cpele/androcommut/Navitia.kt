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

/**
 * Journey from point to point. See [doc.navitia.io/#journeys](https://doc.navitia.io/#journeys)
 */
data class NavitiaJourney(val sections: List<NavitiaSection>?)

data class NavitiaSection(
    val departure_date_time: String?,
    val duration: Long?,
    val from: NavitiaPlace?,
    val to: NavitiaPlace?,
    val type: String?,
    val mode: String?,
    val display_informations: NavitiaDisplayInformations?,
    val transfer_type: String?
)

data class NavitiaDisplayInformations(
    val commercial_mode: String?,
    val code: String?
)

data class NavitiaPlacesResult(val places: List<NavitiaPlace>?)

data class NavitiaPlace(val id: String?, val name: String?)