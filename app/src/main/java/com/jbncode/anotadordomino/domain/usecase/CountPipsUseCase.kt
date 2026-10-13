package com.jbncode.anotadordomino.domain.usecase

import android.graphics.Bitmap
import com.jbncode.anotadordomino.domain.ml.PipCounter
import javax.inject.Inject

class CountPipsUseCase @Inject constructor(private val counter: PipCounter) {
    suspend operator fun invoke(bitmap: Bitmap) = counter.count(bitmap)
}