package com.jbncode.anotadordomino.ui.util

import android.util.Log
import com.google.android.gms.ads.LoadAdError

object AdLog {
    private const val TAG = "AdMob"

    fun d(msg: String) = Log.d(TAG, msg)

    fun loaded(type: String, unitId: String) = Log.d(TAG, "[$type] CARGADO  id=...${unitId.takeLast(10)}")

    fun failedLoad(type: String, unitId: String, e: LoadAdError) =
        Log.w(TAG, "[$type] FALLÓ al cargar  id=...${unitId.takeLast(10)}  code=${e.code}  ${e.message}  ${hint(e.code)}")

    fun shown(type: String) = Log.d(TAG, "[$type] MOSTRADO")
    fun dismissed(type: String) = Log.d(TAG, "[$type] CERRADO")
    fun failedShow(type: String, msg: String) = Log.w(TAG, "[$type] FALLÓ al mostrar: $msg")
    fun skipped(type: String, reason: String) = Log.d(TAG, "[$type] OMITIDO: $reason")

    private fun hint(code: Int) = when (code) {
        0 -> "→ App ID/unidad mal configurados"
        1 -> "→ ID de unidad inválido"
        2 -> "→ sin red"
        3 -> "→ sin anuncio (no fill) o formato de unidad incorrecto"
        else -> ""
    }
}