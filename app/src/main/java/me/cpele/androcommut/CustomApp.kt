package me.cpele.androcommut

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomApp : Application() {

    lateinit var navitiaService: NavitiaService

    override fun onCreate() {
        super.onCreate()
        instance = this

        navitiaService = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.navitia.io")
            .build()
            .create(NavitiaService::class.java)
    }

    companion object {
        lateinit var instance: CustomApp
    }
}
