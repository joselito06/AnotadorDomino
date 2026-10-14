package com.jbncode.anotadordomino.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.R
import com.jbncode.anotadordomino.data.ml.BitmapLoader
import com.jbncode.anotadordomino.domain.model.PipDetection
import com.jbncode.anotadordomino.domain.usecase.CountPipsUseCase
import com.jbncode.anotadordomino.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface PipScanState {
    data object Idle : PipScanState
    data object Processing : PipScanState
    data class Review(val bitmap: Bitmap, val detections: List<PipDetection>, val isCropped: Boolean = false) : PipScanState
    data class Error(val message: UiText) : PipScanState
}

@HiltViewModel
class PipScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val countPips: CountPipsUseCase
) : ViewModel() {

    private companion object {
        const val MAX_PIPS = 168   // doble seis: 28 fichas, 168 puntos como máximo
    }

    private val _state = MutableStateFlow<PipScanState>(PipScanState.Idle)
    val state: StateFlow<PipScanState> = _state.asStateFlow()

    private var original: Bitmap? = null

    /*fun onImageReady(uri: Uri) {
        _state.value = PipScanState.Processing
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) { BitmapLoader.load(context, uri) }
                val result = countPips(bitmap)
                _state.value =
                    if (result.total > MAX_PIPS)
                        PipScanState.Error(UiText.StringResource(R.string.scan_error_too_many, result.total))
                    else
                        PipScanState.Review(bitmap, result.pips)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = PipScanState.Error(UiText.StringResource(R.string.scan_error))
            }
        }
    }*/

    fun onImageReady(uri: Uri) {
        _state.value = PipScanState.Processing
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) { BitmapLoader.load(context, uri) }
                original = bitmap
                runCount(bitmap, cropped = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = PipScanState.Error(UiText.StringResource(R.string.scan_error))
            }
        }
    }

    fun onCropped(cropped: Bitmap) {
        _state.value = PipScanState.Processing
        viewModelScope.launch {
            try {
                runCount(cropped, cropped = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = PipScanState.Error(UiText.StringResource(R.string.scan_error))
            }
        }
    }

    fun restoreOriginal() {
        val bmp = original ?: return
        _state.value = PipScanState.Processing
        viewModelScope.launch {
            try {
                runCount(bmp, cropped = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = PipScanState.Error(UiText.StringResource(R.string.scan_error))
            }
        }
    }

    private suspend fun runCount(bitmap: Bitmap, cropped: Boolean) {
        val result = countPips(bitmap)
        _state.value =
            if (result.total > MAX_PIPS)
                PipScanState.Error(UiText.StringResource(R.string.scan_error_too_many, result.total))
            else
                PipScanState.Review(bitmap, result.pips, isCropped = cropped)
    }

    fun reset() {
        original = null
        _state.value = PipScanState.Idle
    }


}