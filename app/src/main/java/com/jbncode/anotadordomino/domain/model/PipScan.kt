package com.jbncode.anotadordomino.domain.model

/** x, y = centro de la caja, en píxeles de la imagen original. */
data class PipDetection(
    val x: Float, val y: Float, val confidence: Float,
    val w: Float, val h: Float
)

data class PipScanResult(val pips: List<PipDetection>) {
    val total: Int get() = pips.size
}
