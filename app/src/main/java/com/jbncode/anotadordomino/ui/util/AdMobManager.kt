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

object AdMobManager {
    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    // 1. Carga el anuncio en la memoria
    fun loadInterstitial(context: Context, adUnitId: String) {
        if (mInterstitialAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdMob", "Error cargando intersticial: ${adError.message}")
                mInterstitialAd = null
                isLoading = false
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("AdMob", "Intersticial cargado con éxito")
                mInterstitialAd = interstitialAd
                isLoading = false
            }
        })
    }

    // 2. Muestra el anuncio y ejecuta una acción cuando se cierra
    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null // Limpiamos el anuncio usado
                    onAdDismissed() // Navegamos a la siguiente pantalla (ej. Historial)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    onAdDismissed()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            // Si no hay anuncio cargado, no bloqueamos al usuario
            Log.d("AdMob", "El intersticial no estaba listo")
            onAdDismissed()
        }
    }
}