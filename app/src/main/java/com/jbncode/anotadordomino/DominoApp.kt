package com.jbncode.anotadordomino

import android.app.Application

import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DominoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar el SDK de AdMob en un hilo en segundo plano
        Thread {
            MobileAds.initialize(this)
        }.start()
    }
}