package com.jbncode.anotadordomino.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.positionChange
import com.jbncode.anotadordomino.ui.util.AdGate

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
                    AdGate.suppressNextAppOpen = true
                    try { cameraLauncher.launch(uri) } catch (_: ActivityNotFoundException) { AdGate.suppressNextAppOpen = false }
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.scan_from_gallery)) },
                leadingIcon = { Icon(Icons.Default.PhotoLibrary, null) },
                onClick = {
                    menuOpen = false
                    AdGate.suppressNextAppOpen = true
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
            isCropped = s.isCropped,
            onConfirm = { total -> onTotal(total); viewModel.reset() },
            onCrop = viewModel::onCropped,
            onRestore = viewModel::restoreOriginal,
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

private enum class ScanMode { MOVE, ADD, ERASE, CROP }

private data class Mark(val x: Float, val y: Float, val w: Float, val h: Float, val on: Boolean = true)

@Composable
private fun PipReviewDialog(
    bitmap: Bitmap,
    detections: List<PipDetection>,
    isCropped: Boolean,
    onConfirm: (Int) -> Unit,
    onCrop: (Bitmap) -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.kineticColors
    val density = LocalDensity.current
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    val bw = bitmap.width.toFloat()
    val bh = bitmap.height.toFloat()

    val typical = remember(detections) {
        detections.map { (it.w + it.h) / 2 }.sorted().let { if (it.isEmpty()) 24f else it[it.size / 2] }
    }

    var marks by remember(bitmap, detections) {
        mutableStateOf(detections.map { Mark(it.x, it.y, it.w, it.h) })
    }
    val history = remember(bitmap, detections) { mutableStateListOf<List<Mark>>() }

    var mode by remember { mutableStateOf(ScanMode.MOVE) }
    var box by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var selStart by remember { mutableStateOf<Offset?>(null) }   // en pantalla
    var selEnd by remember { mutableStateOf(Offset.Zero) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }     // en píxeles de imagen

    val slopPx = with(density) { 14.dp.toPx() }
    val strokePx = with(density) { 2.dp.toPx() }

    // ── Geometría (leen estado al llamarse, así que siempre ven valores actuales) ──
    fun baseScale(): Float =
        if (box.width == 0 || box.height == 0) 1f else min(box.width / bw, box.height / bh)

    fun curScale(): Float = baseScale() * zoom

    fun toImg(p: Offset): Offset {
        val s = curScale()
        return Offset((p.x - pan.x) / s, (p.y - pan.y) / s)
    }

    fun clampPan() {
        val s = curScale()
        val margin = 80f
        val loX = margin - bw * s
        val hiX = (box.width - margin).coerceAtLeast(loX)
        val loY = margin - bh * s
        val hiY = (box.height - margin).coerceAtLeast(loY)
        pan = Offset(pan.x.coerceIn(loX, hiX), pan.y.coerceIn(loY, hiY))
    }

    fun fit() {
        zoom = 1f
        val b = baseScale()
        pan = Offset((box.width - bw * b) / 2f, (box.height - bh * b) / 2f)
    }

    LaunchedEffect(box, bitmap) { if (box.width > 0) fit() }

    // ── Edición ───────────────────────────────────────────────────────────
    fun commit(new: List<Mark>) { history.add(marks); marks = new }
    fun undo() { if (history.isNotEmpty()) marks = history.removeAt(history.lastIndex) }

    fun changeMode(m: ScanMode) {
        mode = m; selStart = null; cropRect = null
    }

    fun hitIndex(screen: Offset): Int {
        val img = toImg(screen)
        val slop = slopPx / curScale()
        var best = -1
        var bestD = Float.MAX_VALUE
        marks.forEachIndexed { i, m ->
            val dx = abs(img.x - m.x)
            val dy = abs(img.y - m.y)
            if (dx <= m.w / 2 + slop && dy <= m.h / 2 + slop) {
                val d = dx * dx + dy * dy
                if (d < bestD) { best = i; bestD = d }
            }
        }
        return best
    }

    fun handleTap(p: Offset) {
        when (mode) {
            ScanMode.ERASE -> {
                val hit = hitIndex(p)
                if (hit >= 0) {
                    commit(marks.toMutableList().also { it.removeAt(hit) })
                }
            }
            ScanMode.ADD -> {
                // Si tocas encima de un cuadro existente no añade otro encima
                if (hitIndex(p) < 0) {
                    val i = toImg(p)
                    if (i.x in 0f..bw && i.y in 0f..bh) {
                        commit(marks + Mark(i.x, i.y, typical, typical))
                    }
                }
            }
            ScanMode.MOVE, ScanMode.CROP -> Unit
        }
    }

    fun handleTransform(centroid: Offset, panChange: Offset, zoomChange: Float) {
        selStart = null                                   // cancela selección en curso
        val newZoom = (zoom * zoomChange).coerceIn(0.8f, 12f)
        val k = newZoom / zoom
        pan = centroid - (centroid - pan) * k + panChange
        zoom = newZoom
        clampPan()
    }

    fun handleDragStart(p: Offset) {
        if (mode == ScanMode.ERASE || mode == ScanMode.CROP) { selStart = p; selEnd = p }
    }

    fun handleDrag(current: Offset, delta: Offset) {
        if (mode == ScanMode.ERASE || mode == ScanMode.CROP) selEnd = current
        else { pan += delta; clampPan() }
    }

    fun handleDragEnd(end: Offset) {
        val start = selStart ?: return
        selStart = null
        val a = toImg(start)
        val b = toImg(end)
        val l = min(a.x, b.x).coerceIn(0f, bw)
        val r = max(a.x, b.x).coerceIn(0f, bw)
        val t = min(a.y, b.y).coerceIn(0f, bh)
        val bt = max(a.y, b.y).coerceIn(0f, bh)
        when (mode) {
            ScanMode.ERASE -> {
                val kept = marks.filterNot { it.x in l..r && it.y in t..bt }
                if (kept.size != marks.size) commit(kept)
            }
            ScanMode.CROP -> cropRect = Rect(l, t, r, bt)
            else -> Unit
        }
    }

    fun applyCrop() {
        val r = cropRect ?: return
        val x = r.left.roundToInt().coerceIn(0, bitmap.width - 1)
        val y = r.top.roundToInt().coerceIn(0, bitmap.height - 1)
        val w = r.width.roundToInt().coerceAtMost(bitmap.width - x)
        val h = r.height.roundToInt().coerceAtMost(bitmap.height - y)
        if (w >= 32 && h >= 32) onCrop(Bitmap.createBitmap(bitmap, x, y, w, h))
    }

    // ── UI ────────────────────────────────────────────────────────────────
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp)).background(colors.dialogBackground).padding(12.dp)
        ) {
            // Barra de modos
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ModeChip(Icons.Default.PanTool, stringResource(R.string.scan_mode_move),
                    mode == ScanMode.MOVE, Modifier.weight(1f)) { changeMode(ScanMode.MOVE) }
                ModeChip(Icons.Default.AddCircleOutline, stringResource(R.string.scan_mode_add),
                    mode == ScanMode.ADD, Modifier.weight(1f)) { changeMode(ScanMode.ADD) }
                ModeChip(Icons.Default.DeleteSweep, stringResource(R.string.scan_mode_erase),
                    mode == ScanMode.ERASE, Modifier.weight(1f)) { changeMode(ScanMode.ERASE) }
                ModeChip(Icons.Default.Crop, stringResource(R.string.scan_mode_crop),
                    mode == ScanMode.CROP, Modifier.weight(1f)) { changeMode(ScanMode.CROP) }
            }
            Spacer(Modifier.height(8.dp))

            // Lienzo
            Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
                Canvas(
                    Modifier.fillMaxSize()
                        .clipToBounds()
                        .onSizeChanged { box = it }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val slop = viewConfiguration.touchSlop
                                var moved = false
                                var multi = false
                                var current = down.position
                                do {
                                    val event = awaitPointerEvent()
                                    val pressed = event.changes.count { it.pressed }
                                    if (pressed >= 2) {
                                        multi = true
                                        handleTransform(
                                            event.calculateCentroid(),
                                            event.calculatePan(),
                                            event.calculateZoom()
                                        )
                                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                                    } else if (!multi) {
                                        val change = event.changes.firstOrNull() ?: break
                                        current = change.position
                                        if (!moved && (current - down.position).getDistance() > slop) {
                                            moved = true
                                            handleDragStart(down.position)
                                        }
                                        if (moved) {
                                            handleDrag(current, change.positionChange())
                                            change.consume()
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                if (!multi) {
                                    if (moved) handleDragEnd(current) else handleTap(down.position)
                                }
                            }
                        }
                ) {
                    val s = curScale()

                    drawImage(
                        image = image,
                        dstOffset = IntOffset(pan.x.roundToInt(), pan.y.roundToInt()),
                        dstSize = IntSize((bw * s).roundToInt(), (bh * s).roundToInt()),
                        filterQuality = FilterQuality.Medium
                    )

                    marks.forEach { m ->
                        drawRect(
                            color = Color(0xFF00E676),
                            topLeft = Offset(pan.x + (m.x - m.w / 2) * s, pan.y + (m.y - m.h / 2) * s),
                            size = Size(m.w * s, m.h * s),
                            style = Stroke(width = strokePx)
                        )
                    }

                    // Área de recorte confirmada (oscurece el resto)
                    cropRect?.let { r ->
                        val tl = Offset(pan.x + r.left * s, pan.y + r.top * s)
                        val sz = Size(r.width * s, r.height * s)
                        clipRect(tl.x, tl.y, tl.x + sz.width, tl.y + sz.height, ClipOp.Difference) {
                            drawRect(Color.Black.copy(alpha = 0.55f))
                        }
                        drawRect(Color(0xFF5CF0E0), tl, sz, style = Stroke(width = strokePx))
                    }

                    // Selección en curso (borrar o recortar)
                    selStart?.let { st ->
                        val tl = Offset(min(st.x, selEnd.x), min(st.y, selEnd.y))
                        val sz = Size(abs(selEnd.x - st.x), abs(selEnd.y - st.y))
                        val c = if (mode == ScanMode.ERASE) Color(0xFFFF5252) else Color(0xFF5CF0E0)
                        drawRect(c.copy(alpha = 0.18f), tl, sz)
                        drawRect(c, tl, sz, style = Stroke(width = strokePx))
                    }
                }

                // Deshacer y reencuadrar, flotando sobre la imagen
                Row(
                    Modifier.align(Alignment.TopEnd).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FloatingTool(Icons.Default.ZoomOutMap, true) { fit() }
                    FloatingTool(Icons.AutoMirrored.Filled.Undo, history.isNotEmpty()) { undo() }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    when (mode) {
                        ScanMode.MOVE -> R.string.scan_hint_move
                        ScanMode.ADD -> R.string.scan_hint_add
                        ScanMode.ERASE -> R.string.scan_hint_erase
                        ScanMode.CROP -> R.string.scan_hint_crop
                    }
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            if (mode == ScanMode.CROP && cropRect != null) {
                Box(
                    Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(12.dp))
                        .background(colors.cyanAccent).clickable { applyCrop() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.scan_crop_apply),
                        color = MaterialTheme.colorScheme.background, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (isCropped) {
                Text(
                    stringResource(R.string.scan_restore_original),
                    color = colors.cyanAccent, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp)).clickable { onRestore() }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            val total = marks.size
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
            Text(
                stringResource(R.string.scan_cancel),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp)).clickable(onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ModeChip(
    icon: ImageVector, label: String, selected: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val colors = MaterialTheme.kineticColors
    val fg = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.cyanAccent else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Text(label, color = fg, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun FloatingTool(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape)
            .background(Color.Black.copy(alpha = if (enabled) 0.6f else 0.3f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = if (enabled) 1f else 0.4f), modifier = Modifier.size(20.dp))
    }
}