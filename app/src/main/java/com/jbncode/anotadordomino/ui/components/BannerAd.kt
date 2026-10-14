package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jbncode.anotadordomino.R
import com.jbncode.anotadordomino.ui.util.AdLog

@Composable
fun BannerAd(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val unitId = stringResource(R.string.banner_ad_id)
    val widthDp = LocalConfiguration.current.screenWidthDp

    val adView = remember(unitId, widthDp) {
        AdView(context).apply {
            adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() { AdLog.loaded("Banner", unitId) }
                override fun onAdFailedToLoad(e: com.google.android.gms.ads.LoadAdError) {
                    AdLog.failedLoad("Banner", unitId, e)
                }

                override fun onAdImpression() {
                    //super.onAdImpression()
                    AdLog.shown("Banner")
                }
            }

            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
            adUnitId = unitId
            //android.util.Log.d("AdMob", "Banner unitId = $unitId")
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) { onDispose { adView.destroy() } }

    AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })
}