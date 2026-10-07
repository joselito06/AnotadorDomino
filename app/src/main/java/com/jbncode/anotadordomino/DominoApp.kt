package com.jbncode.anotadordomino

import android.app.Application

import com.google.android.gms.ads.MobileAds
import com.jbncode.anotadordomino.util.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DominoApp : Application() {
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)

        appOpenAdManager = AppOpenAdManager(this)

        appOpenAdManager.loadAd(this)
    }
}