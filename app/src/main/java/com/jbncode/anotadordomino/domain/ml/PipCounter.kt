package com.jbncode.anotadordomino.domain.ml

import android.graphics.Bitmap
import com.jbncode.anotadordomino.domain.model.PipScanResult

interface PipCounter {
    suspend fun count(bitmap: Bitmap): PipScanResult
}