package com.jbncode.anotadordomino.ui.util

import android.app.Activity
import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.jbncode.anotadordomino.util.AppOpenAdManager
import java.util.concurrent.atomic.AtomicBoolean

object AdsBootstrap {
    private val started = AtomicBoolean(false)
    private var appOpen: AppOpenAdManager? = null

    /** Pide consentimiento y solo entonces inicializa los anuncios. */
    fun start(activity: Activity) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        val app = activity.application

        info.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    if (info.canRequestAds()) init(app)
                }
            },
            { if (info.canRequestAds()) init(app) }
        )
        // Usuarios que ya dieron consentimiento antes: no esperes al formulario
        if (info.canRequestAds()) init(app)
    }

    private fun init(app: Application) {
        if (!started.compareAndSet(false, true)) return
        MobileAds.initialize(app)
        appOpen = AppOpenAdManager(app).also { it.loadAd(app) }
    }
}