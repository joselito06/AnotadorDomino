package com.jbncode.anotadordomino.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jbncode.anotadordomino.ui.util.AdLog

object AdMobManager {

    private var lastShownAt = 0L
    private var finishedGames = 0
    private const val EVERY_N_GAMES = 2
    private const val MIN_INTERVAL_MS = 3 * 60 * 1000L // 3 minutos
    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    // 1. Carga el anuncio en la memoria
    fun loadInterstitial(context: Context, adUnitId: String) {
        if (mInterstitialAd != null) { AdLog.d("[Intersticial] ya hay uno cargado"); return }
        if ( isLoading) { AdLog.d("[Intersticial] carga en curso"); return }

        isLoading = true
        AdLog.d("[Intersticial] solicitando... id=...${adUnitId.takeLast(10)}")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                AdLog.failedLoad("Intersticial", adUnitId, adError)
                mInterstitialAd = null
                isLoading = false
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                AdLog.loaded("Intersticial", adUnitId)
                mInterstitialAd = interstitialAd
                isLoading = false
            }
        })
    }

    // 2. Muestra el anuncio y ejecuta una acción cuando se cierra
    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        finishedGames++
        val now = android.os.SystemClock.elapsedRealtime()
        val allowed = finishedGames % EVERY_N_GAMES == 0 &&
                (lastShownAt == 0L || now - lastShownAt >= MIN_INTERVAL_MS)

        val ad = mInterstitialAd
        if (!allowed || ad == null) {
            AdLog.skipped(
                "Intersticial",
                if (ad == null) "no estaba cargado" else "regla de frecuencia (partida #$finishedGames)"
            )
            onAdDismissed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { AdLog.shown("Intersticial"); lastShownAt = android.os.SystemClock.elapsedRealtime() }
            override fun onAdDismissedFullScreenContent() { AdLog.dismissed("Intersticial"); mInterstitialAd = null; onAdDismissed() }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) { AdLog.failedShow("Intersticial", adError.message); mInterstitialAd = null; onAdDismissed() }
        }
        ad.show(activity)
    }
}