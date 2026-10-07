package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jbncode.anotadordomino.R

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    // Obtenemos el ID del banner desde tus recursos (generado por build.gradle)
    val adUnitId = stringResource(id = R.string.banner_ad_id)

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                // Definimos el tamaño del banner
                setAdSize(AdSize.BANNER)
                // Le asignamos el ID
                this.adUnitId = adUnitId
                // Cargamos el anuncio
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}