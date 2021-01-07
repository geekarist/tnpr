package me.cpele.androcommut

import android.app.Application
import android.util.LruCache
import me.cpele.androcommut.core.Trip
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomApp : Application() {

    lateinit var tripCache: LruCache<String, Trip>
        private set

    lateinit var navitiaService: NavitiaService
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        navitiaService = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.navitia.io")
            .build()
            .create(NavitiaService::class.java)

        tripCache = LruCache<String, Trip>(100) // TODO: Set maxSize to 1 MB
    }

    companion object {
        lateinit var instance: CustomApp
    }
}
