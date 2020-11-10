package me.cpele.androcommut

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NavitiaService {

    @GET("/v1/coverage/fr-idf/places")
    suspend fun places(
        @Header("Authorization") auth: String,
        @Query("q") q: String?
    ): NavitiaPlacesResult
}

data class NavitiaPlacesResult(val places: List<NavitiaPlace>)

data class NavitiaPlace(val id: String?, val name: String?, val label: String?)