package com.jbncode.anotadordomino.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.R
import com.jbncode.anotadordomino.domain.model.PipDetection
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.PipScanState
import com.jbncode.anotadordomino.ui.viewmodel.PipScanViewModel
import java.io.File
import kotlin.math.abs

private fun createScanUri(context: Context): Uri {
    val dir = File(context.cacheDir, "scans").apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() }              // limpia fotos anteriores
    val file = File.createTempFile("scan_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

/**
 * Botón de cámara + todo el flujo (captura, procesado, revisión).
 * Llama a [onTotal] con el total de puntos confirmado por el usuario.
 */
@Composable
fun PipScanControl(
    onTotal: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PipScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colors = MaterialTheme.kineticColors
    val state by viewModel.state.collectAsStateWithLifecycle()

    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) pendingUri?.let(viewModel::onImageReady)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onImageReady)
    }

    // ── Botón con menú ────────────────────────────────────────────────────
    Box(modifier) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                .background(colors.cyanAccent.copy(alpha = 0.15f))
                .clickable { menuOpen = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CameraAlt, "Scan", tint = colors.cyanAccent, modifier = Modifier.size(24.dp))
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.scan_take_photo)) },
                leadingIcon = { Icon(Icons.Default.CameraAlt, null) },
                onClick = {
                    menuOpen = false
                    val uri = createScanUri(context)
                    pendingUri = uri
                    try { cameraLauncher.launch(uri) } catch (_: ActivityNotFoundException) { }
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.scan_from_gallery)) },
                leadingIcon = { Icon(Icons.Default.PhotoLibrary, null) },
                onClick = {
                    menuOpen = false
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }
    }

    // ── Estados ───────────────────────────────────────────────────────────
    when (val s = state) {
        PipScanState.Idle -> Unit

        PipScanState.Processing -> Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Column(
                Modifier.clip(RoundedCornerShape(20.dp)).background(colors.dialogBackground).padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = colors.neonGreen)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.scan_processing), color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium)
            }
        }

        is PipScanState.Review -> PipReviewDialog(
            bitmap = s.bitmap,
            detections = s.detections,
            onConfirm = { total -> onTotal(total); viewModel.reset() },
            onDismiss = viewModel::reset
        )

        is PipScanState.Error -> AlertDialog(
            onDismissRequest = viewModel::reset,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            text = { Text(s.message.asString(), color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = viewModel::reset) {
                    Text(stringResource(R.string.settings_dialog_close), color = colors.cyanAccent)
                }
            }
        )
    }
}

// ── Diálogo de revisión ───────────────────────────────────────────────────────

private data class Mark(val x: Float, val y: Float, val w: Float, val h: Float, val on: Boolean = true)

@Composable
private fun PipReviewDialog(
    bitmap: Bitmap,
    detections: List<PipDetection>,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.kineticColors
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    val marks = remember(detections) {
        mutableStateListOf<Mark>().apply { addAll(detections.map { Mark(it.x, it.y, it.w, it.h) }) }
    }
    val typical = remember(detections) {
        detections.map { (it.w + it.h) / 2 }.sorted().let { if (it.isEmpty()) 24f else it[it.size / 2] }
    }
    val total = marks.count { it.on }
    val ratio = bitmap.width.toFloat() / bitmap.height

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(0.94f).clip(RoundedCornerShape(24.dp))
                .background(colors.dialogBackground).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.scan_review_hint), color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))

            Box(Modifier.fillMaxWidth().heightIn(max = 460.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.aspectRatio(ratio).pointerInput(bitmap) {
                        detectTapGestures { tap ->
                            val scale = size.width / bitmap.width.toFloat()
                            val ix = tap.x / scale
                            val iy = tap.y / scale
                            val slop = 10.dp.toPx() / scale
                            val hit = marks.indexOfLast {
                                abs(ix - it.x) <= it.w / 2 + slop && abs(iy - it.y) <= it.h / 2 + slop
                            }
                            if (hit >= 0) marks[hit] = marks[hit].copy(on = !marks[hit].on)
                            else marks.add(Mark(ix, iy, typical, typical))
                        }
                    }
                ) {
                    Image(image, null, contentScale = ContentScale.FillBounds, modifier = Modifier.matchParentSize())
                    Canvas(Modifier.matchParentSize()) {
                        val scale = size.width / bitmap.width
                        marks.forEach { m ->
                            drawRect(
                                color = if (m.on) Color(0xFF00E676) else Color(0xFFFF5252),
                                topLeft = Offset((m.x - m.w / 2) * scale, (m.y - m.h / 2) * scale),
                                size = Size(m.w * scale, m.h * scale),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val canUse = total > 0
            Box(
                Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (canUse) colors.neonGreen else colors.neonGreen.copy(alpha = 0.3f))
                    .clickable(enabled = canUse) { onConfirm(total) },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.scan_use_total, total),
                    color = MaterialTheme.colorScheme.background, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.scan_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 8.dp))
        }
    }
}