package com.jbncode.anotadordomino.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jbncode.anotadordomino.R
import com.jbncode.anotadordomino.domain.model.AvatarType
import com.jbncode.anotadordomino.ui.components.KineticTopBar
import com.jbncode.anotadordomino.ui.components.RecruitPlayerDialog
import com.jbncode.anotadordomino.ui.viewmodel.PlayerUiState
import com.jbncode.anotadordomino.ui.viewmodel.SetupViewModel
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.SetupTutorialStep
import androidx.core.net.toUri
import com.jbncode.anotadordomino.ui.util.UiText

@Composable
fun SetupScreen(
    onSettingsClick: () -> Unit = {},
    onGameStarted: (Int) -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val colors      = MaterialTheme.kineticColors
    val targetScore by viewModel.targetScore.collectAsStateWithLifecycle()
    val isPairsMode by viewModel.isPairsMode.collectAsStateWithLifecycle()
    val players     by viewModel.quickPlayers.collectAsStateWithLifecycle()
    val isLoading   by viewModel.isLoading.collectAsStateWithLifecycle()
    val canStart    by viewModel.canStartGame.collectAsStateWithLifecycle()
    val canAdd      by viewModel.canAddPlayer.collectAsStateWithLifecycle()
    val hint        by viewModel.playerHint.collectAsStateWithLifecycle()

    val tutorialStep by viewModel.tutorialStep.collectAsStateWithLifecycle()

    var showDialog  by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var newBtnBounds by remember { mutableStateOf(Rect.Zero) }
    var startBtnBounds by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            KineticTopBar(onSettingsClick = onSettingsClick)
            SetupSeasonBanner()
            Spacer(Modifier.height(24.dp))

            SetupSectionLabel(stringResource(R.string.setup_game_mode), Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(10.dp))
            SetupGameModeToggle(
                isPairs  = isPairsMode,
                onSelect = { viewModel.updateGameMode(it) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                SetupSectionLabel(stringResource(R.string.setup_point_goal))
                Text(
                    text  = targetScore.toString(),
                    color = colors.neonGreen,
                    style = MaterialTheme.typography.displaySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            SetupPointGoalSlider(
                value         = targetScore.toFloat(),
                onValueChange = { viewModel.updateTargetScore(it) },
                modifier      = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(28.dp))

            // Header de jugadores con contador
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SetupSectionLabel(stringResource(R.string.setup_quick_add_players))
                    Spacer(Modifier.width(8.dp))
                    // Contador: ej. "2/2" o "1/4"
                    val limits = if (isPairsMode) "/${2}" else "/${4}"
                    Text(
                        text  = "${players.size}$limits",
                        color = if (canStart) colors.neonGreen
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                SetupNewButton(
                    enabled = canAdd,
                    modifier = Modifier.onGloballyPositioned { newBtnBounds = it.boundsInRoot() },
                    onClick = { showDialog = true }
                )
            }
            Spacer(Modifier.height(12.dp))

            SetupPlayersGrid(
                players  = players,
                onRemove = { viewModel.removeQuickPlayer(it) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            // Hint contextual animado
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(
                visible  = hint.asString().isNotBlank(),
                enter    = fadeIn(),
                exit     = fadeOut(),
                modifier = Modifier.padding(horizontal = 22.dp)
            ) {
                Text(
                    text      = hint.asString(),
                    color     = if (hint.asString().startsWith("✓")) colors.neonGreen
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style     = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(24.dp))

            SetupStartMatchButton(
                enabled   = canStart,
                isLoading = isLoading,
                modifier  = Modifier
                    .padding(horizontal = 20.dp)
                    .onGloballyPositioned { startBtnBounds = it.boundsInRoot() },
                onClick   = { viewModel.startGame(onGameStarted) }
            )
        }

        if (showDialog) {
            RecruitPlayerDialog(
                onDismiss  = { showDialog = false },
                onAddPlayer = { name, avatarType, photoUri ->
                    viewModel.addQuickPlayer(name, avatarType, photoUri)
                    showDialog = false
                }
            )
        }

        TutorialOverlay(
            step = tutorialStep,
            newBtnBounds = newBtnBounds,
            startBtnBounds = startBtnBounds
        )
    }
}

// --- COMPONENTE DE AYUDA SUPERPUESTA MEJORADO ---
@Composable
fun TutorialOverlay(
    step: SetupTutorialStep,
    newBtnBounds: Rect,
    startBtnBounds: Rect
) {
    val density = LocalDensity.current
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val colors = MaterialTheme.kineticColors

    // Animación de rebote (siempre positiva para no meterse en el botón)
    val infiniteTransition = rememberInfiniteTransition(label = "tutorial")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce"
    )

    Box(Modifier.fillMaxSize()) {
        // --- PASO 1: SEÑALAR EL BOTON "+ NEW" ---
        if (step == SetupTutorialStep.ADD_PLAYERS && newBtnBounds != Rect.Zero) {

            // Colocamos el dedo justo DEBAJO del botón, no encima.
            val fingerX = with(density) { newBtnBounds.center.x.toDp() - 34.dp }
            val fingerY = with(density) { newBtnBounds.bottom.toDp()  - 22.dp } // 4dp de margen debajo

            Icon(
                Icons.Default.TouchApp,
                contentDescription = null,
                tint = colors.cyanAccent,
                modifier = Modifier
                    .offset(x = fingerX, y = fingerY - bounce.dp)
                    .size(32.dp)
            )

        }

        // --- PASO 2: SEÑALAR EL BOTON "START MATCH" ---
        if (step == SetupTutorialStep.START_MATCH && startBtnBounds != Rect.Zero) {
            val isVisible = startBtnBounds.top > 0 && startBtnBounds.top < screenHeightPx - 100f

            if (isVisible) {
                // Flecha apuntando al botón desde ARRIBA
                val arrowY = with(density) { startBtnBounds.top.toDp() - 56.dp }
                val arrowX = with(density) { startBtnBounds.center.x.toDp() - 12.dp }

                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = colors.neonGreen,
                    modifier = Modifier
                        .offset(x = arrowX, y = arrowY + bounce.dp)
                        .size(24.dp)
                )
            } else {
                // Si hay que hacer scroll (pantallas pequeñas)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                        .offset(y = bounce.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = colors.cyanAccent, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(4.dp))
                        TutorialTooltip(stringResource(R.string.setup_tutorial_scroll))
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialTooltip(text: String) {
    Box(
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .background(Color(0xFF080808), RoundedCornerShape(12.dp)) // Fondo oscuro
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.kineticColors.neonGreen,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

// ── Season Banner ──────────────────────────────────────────────────────────────

@Composable
private fun SetupSeasonBanner() {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0D2B2B), Color(0xFF0A1A1A), Color(0xFF061515))
                )
            )
    ) {
        Box(Modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(Color(0xFF0E4040).copy(alpha = 0.6f), Color.Transparent),
                radius = 500f
            )
        ))
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(stringResource(R.string.setup_season_label), color = colors.neonGreen,
                style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.setup_season_name), color = Color.White,
                style = MaterialTheme.typography.headlineLarge)
        }
    }
}

// ── Section Label ──────────────────────────────────────────────────────────────

@Composable
private fun SetupSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        style    = MaterialTheme.typography.labelMedium,
        modifier = modifier
    )
}

// ── Game Mode Toggle ───────────────────────────────────────────────────────────

@Composable
private fun SetupGameModeToggle(
    isPairs: Boolean,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors  = MaterialTheme.kineticColors
    val options = listOf(Icons.Default.Group to stringResource(R.string.setup_mode_pairs), Icons.Default.Person to stringResource(R.string.setup_mode_individual))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, (icon, label) ->
                val isSelected = if (index == 0) isPairs else !isPairs
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isSelected) colors.cyanAccent else Color.Transparent)
                        .clickable { onSelect(index == 0) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, label,
                            tint = if (isSelected) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(label,
                            color = if (isSelected) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

// ── Slider ─────────────────────────────────────────────────────────────────────

@Composable
private fun SetupPointGoalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors
    val marks   = listOf(50f, 100f, 200f, 500f)
    val minVal  = 50f
    val maxVal  = 500f

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = minVal..maxVal,
            colors        = SliderDefaults.colors(
                thumbColor         = colors.neonGreen,
                activeTrackColor   = colors.neonGreen,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // Marcas alineadas con la posición real del track.
        // El thumb tiene un padding interno de ~10.dp a cada lado en Material3,
        // así que compensamos para que 50 quede bajo el inicio del track
        // y 500 quede bajo el final del track.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val thumbRadius = 10.dp   // radio visual del thumb en M3
            val trackWidth  = maxWidth - (thumbRadius * 2)

            marks.forEach { mark ->
                val fraction = (mark - minVal) / (maxVal - minVal)   // 0f..1f
                val offsetX  = thumbRadius + trackWidth * fraction

                Box(
                    modifier = Modifier.offset(x = offsetX - 12.dp)  // centrar el texto
                ) {
                    Text(
                        text  = mark.toInt().toString(),
                        color = if (value.toInt() == mark.toInt())
                            colors.neonGreen
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ── New Button ─────────────────────────────────────────────────────────────────

@Composable
private fun SetupNewButton(enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, "New",
                tint = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.setup_btn_new),
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ── Players Grid ───────────────────────────────────────────────────────────────

@Composable
private fun SetupPlayersGrid(
    players: List<PlayerUiState>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (players.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = stringResource(R.string.setup_empty_players),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        players.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { player ->
                    SetupPlayerCard(
                        player   = player,
                        onRemove = { onRemove(player.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SetupPlayerCard(
    player: PlayerUiState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors
    val context = LocalContext.current

    // Icono según el tipo de avatar preset
    val presetIcon = when (player.avatarType) {
        AvatarType.PRESET_STAR    -> Icons.Default.Star
        AvatarType.PRESET_CROWN   -> Icons.Default.EmojiEvents
        AvatarType.PRESET_BOLT    -> Icons.Default.FlashOn
        AvatarType.PRESET_FIRE    -> Icons.Default.Whatshot
        AvatarType.PRESET_DIAMOND -> Icons.Default.Diamond
        AvatarType.PRESET_SHIELD  -> Icons.Default.Shield
        AvatarType.PRESET_KING    -> Icons.Default.AccountCircle
        AvatarType.PRESET_NINJA   -> Icons.Default.SelfImprovement
        AvatarType.PRESET_ROBOT   -> Icons.Default.SmartToy
        AvatarType.PRESET_WIZARD  -> Icons.Default.AutoAwesome
        AvatarType.PRESET_PIRATE  -> Icons.Default.SportsKabaddi
        AvatarType.PRESET_ALIEN   -> Icons.Default.BubbleChart
        AvatarType.GALLERY        -> null

    }
    val presetTint = when (player.avatarType) {
        AvatarType.PRESET_STAR    -> colors.neonGreen
        AvatarType.PRESET_CROWN   -> Color(0xFFD4A800)
        AvatarType.PRESET_BOLT    -> colors.cyanAccent
        AvatarType.PRESET_FIRE    -> Color(0xFFFF5C3A)
        AvatarType.PRESET_DIAMOND -> Color(0xFF7B61FF)
        AvatarType.PRESET_SHIELD  -> colors.cyanAccent
        AvatarType.PRESET_KING    -> Color(0xFFFFAA00)
        AvatarType.PRESET_NINJA   -> Color(0xFF00E5FF)
        AvatarType.PRESET_ROBOT   -> colors.neonGreen
        AvatarType.PRESET_WIZARD  -> Color(0xFFCC44FF)
        AvatarType.PRESET_PIRATE  -> Color(0xFFFF6B35)
        AvatarType.PRESET_ALIEN   -> Color(0xFF44FF88)
        AvatarType.GALLERY        -> colors.cyanAccent

    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.cyanAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (player.avatarType == AvatarType.GALLERY && player.photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(player.photoUri.toUri())
                            .crossfade(true).build()
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier     = Modifier.fillMaxSize().clip(CircleShape)
                )
            }else if(presetIcon != null){
                Icon(presetIcon, null,
                    tint     = presetTint,
                    modifier = Modifier.size(22.dp))
            }

        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall)
            Text(UiText.StringResource(R.string.setup_wins_label, player.wins).asString(),// "WINS: ${player.wins}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
        // X para eliminar el jugador de la lista
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, "Remove",
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp))
        }
    }
}

// ── Start Match Button ─────────────────────────────────────────────────────────

@Composable
private fun SetupStartMatchButton(
    enabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.kineticColors
    val pulse  = rememberInfiniteTransition(label = "glow")
    val glowAlpha by pulse.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.9f,
        label         = "glowAlpha",
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .then(
                // El glow solo aparece cuando está habilitado
                if (enabled) Modifier.shadow(
                    elevation    = 20.dp,
                    shape        = RoundedCornerShape(16.dp),
                    ambientColor = colors.neonGreen.copy(alpha = glowAlpha * 0.4f),
                    spotColor    = colors.neonGreen.copy(alpha = glowAlpha)
                ) else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) colors.neonGreen
                else colors.neonGreen.copy(alpha = 0.22f)
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color       = MaterialTheme.colorScheme.background,
                modifier    = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.setup_btn_start_match),
                    color = if (enabled) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.FlashOn, null,
                    tint = if (enabled) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}