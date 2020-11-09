package me.cpele.afk

import android.app.Application

class CustomApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: CustomApp
    }
}
