package io.navendra.retrofitkotlindeferred.utils

import android.app.Application

class PhinCallApps : Application() {


    override fun onCreate() {
        super.onCreate()
        initServices()



        System.setProperty("javax.net.debug", "TLS")
    }

    fun initServices() {
        InteractionService.getInstance().init(this)
    }
}