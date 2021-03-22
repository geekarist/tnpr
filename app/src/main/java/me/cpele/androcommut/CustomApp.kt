package me.cpele.androcommut

import android.app.Application
import android.util.LruCache
import me.cpele.androcommut.core.Journey
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomApp : Application() {

    lateinit var journeyCache: LruCache<String, Journey>
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

        journeyCache = LruCache<String, Journey>(100)
    }

    companion object {
        lateinit var instance: CustomApp
    }
}
