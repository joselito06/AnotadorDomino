package com.jbncode.anotadordomino.data.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import com.jbncode.anotadordomino.domain.ml.PipCounter
import com.jbncode.anotadordomino.domain.model.PipDetection
import com.jbncode.anotadordomino.domain.model.PipScanResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class YoloxPipCounter @Inject constructor(
    @ApplicationContext private val context: Context
) : PipCounter {

    private companion object {
        const val MODEL_FILE = "pips_yolox.onnx"
        const val S = 640            // debe coincidir con input_size del entrenamiento
        const val SCORE_THR = 0.40f  // = SCORE_THR de tu prueba en Colab
        const val NMS_IOU = 0.25f    // = IOU_THR de tu prueba en Colab
        const val SIZE_TOL = 0.5f    // desviación de tamaño permitida vs. la mediana
        const val PAD_VALUE = 114f
    }

    private val env = OrtEnvironment.getEnvironment()

    private val session: OrtSession by lazy {
        val bytes = context.assets.open(MODEL_FILE).use { it.readBytes() }
        env.createSession(bytes, OrtSession.SessionOptions().apply { setIntraOpNumThreads(4) })
    }

    override suspend fun count(bitmap: Bitmap): PipScanResult = withContext(Dispatchers.Default) {
        val (data, ratio) = preprocess(bitmap)
        val shape = longArrayOf(1, 3, S.toLong(), S.toLong())

        val rows: Array<FloatArray> =
            OnnxTensor.createTensor(env, FloatBuffer.wrap(data), shape).use { input ->
                session.run(mapOf(session.inputNames.first() to input)).use { out ->
                    @Suppress("UNCHECKED_CAST")
                    (out[0].value as Array<Array<FloatArray>>)[0]   // [8400, 6]
                }
            }
        PipScanResult(postprocess(rows, ratio))
    }

    /** Letterbox arriba-izquierda + CHW + BGR, sin normalizar (igual que YOLOX). */
    private fun preprocess(src: Bitmap): Pair<FloatArray, Float> {
        val r = min(S.toFloat() / src.width, S.toFloat() / src.height)
        val w = (src.width * r).roundToInt().coerceAtLeast(1)
        val h = (src.height * r).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(src, w, h, true)
        val px = IntArray(w * h).also { scaled.getPixels(it, 0, w, 0, 0, w, h) }
        if (scaled !== src) scaled.recycle()

        val plane = S * S
        val data = FloatArray(3 * plane) { PAD_VALUE }
        for (y in 0 until h) for (x in 0 until w) {
            val p = px[y * w + x]
            val i = y * S + x
            data[i] = (p and 0xFF).toFloat()                  // B
            data[plane + i] = ((p shr 8) and 0xFF).toFloat()  // G
            data[2 * plane + i] = ((p shr 16) and 0xFF).toFloat() // R
        }
        return data to r
    }

    private fun postprocess(rows: Array<FloatArray>, ratio: Float): List<PipDetection> {
        val candidates = rows.mapNotNull { r ->
            val score = r[4] * r[5]
            if (score < SCORE_THR) null
            else PipDetection(
                x = r[0] / ratio, y = r[1] / ratio, confidence = score,
                w = r[2] / ratio, h = r[3] / ratio
            )
        }.sortedByDescending { it.confidence }

        // NMS + descartar cajas cuyo centro cae dentro de otra ya aceptada
        val kept = mutableListOf<PipDetection>()
        for (d in candidates) {
            if (kept.none { iou(it, d) > NMS_IOU || centerInside(it, d) }) kept += d
        }
        return filterBySize(kept)
    }

    private fun filterBySize(list: List<PipDetection>): List<PipDetection> {
        if (list.size < 6) return list
        val sizes = list.map { (it.w + it.h) / 2 }.sorted()
        val median = sizes[sizes.size / 2]
        return list.filter {
            val s = (it.w + it.h) / 2
            s >= median * (1 - SIZE_TOL) && s <= median * (1 + SIZE_TOL)
        }
    }

    private fun centerInside(outer: PipDetection, d: PipDetection) =
        abs(d.x - outer.x) <= outer.w / 2 && abs(d.y - outer.y) <= outer.h / 2

    private fun iou(a: PipDetection, b: PipDetection): Float {
        val iw = (min(a.x + a.w / 2, b.x + b.w / 2) - max(a.x - a.w / 2, b.x - b.w / 2)).coerceAtLeast(0f)
        val ih = (min(a.y + a.h / 2, b.y + b.h / 2) - max(a.y - a.h / 2, b.y - b.h / 2)).coerceAtLeast(0f)
        val inter = iw * ih
        return inter / (a.w * a.h + b.w * b.h - inter + 1e-6f)
    }
}