package me.cpele.tnpr

import android.app.Application
import android.util.LruCache
import me.cpele.tnpr.core.Journey
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

        val httpClient = OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                val interceptor = HttpLoggingInterceptor()
                val level = HttpLoggingInterceptor.Level.BODY
                interceptor.level = level
                addNetworkInterceptor(interceptor)
            }
        }.build()

        navitiaService = Retrofit.Builder()
            .client(httpClient)
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
