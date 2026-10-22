package com.fresh.launcher.ui

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.fresh.launcher.AppEntry
import com.fresh.launcher.R
import com.fresh.launcher.Weather
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val FavoriteRowHeight = 60.dp

/** Reloj en dos tonos (hora fuerte, minutos teñidos), fecha y clima como datos. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClockBlock(weather: Weather?, fahrenheit: Boolean, weatherEnabled: Boolean, onWeatherClick: () -> Unit) {
    val colors = LocalLauncherColors.current
    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            // Despierta justo al cambiar el minuto
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }

    val locale = LocalConfiguration.current.locales[0]
    val is24 = DateFormat.is24HourFormat(context)
    val hour = now.format(DateTimeFormatter.ofPattern(if (is24) "HH" else "h", locale))
    val minutes = now.format(DateTimeFormatter.ofPattern("mm", locale))
    val datePattern = remember(locale) { DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM") }
    val date = now.format(DateTimeFormatter.ofPattern(datePattern, locale)).uppercase(locale)

    Column {
        BasicText(
            text = buildAnnotatedString {
                append(hour)
                withStyle(SpanStyle(color = colors.onMeshMuted)) { append(":$minutes") }
            },
            style = LauncherType.clock.copy(color = colors.onMesh),
            maxLines = 1,
            modifier = Modifier.semantics { contentDescription = "$hour:$minutes" },
        )
        // FlowRow: con letra grande el clima baja a otra línea en vez de cortarse
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
            // El relleno táctil del clima agranda la fila; se compensa para que la fecha quede pegada al reloj
            modifier = Modifier
                .padding(start = 4.dp)
                .offset(y = (-10).dp),
        ) {
            // El separador va con la fecha: si la fila se parte, queda cerrando la primera línea
            BasicText(
                text = buildAnnotatedString {
                    append(date)
                    withStyle(SpanStyle(color = colors.onMeshMuted)) { append("  ·") }
                },
                style = LauncherType.data.copy(color = colors.onMesh),
                maxLines = 1,
            )
            val weatherText = when {
                !weatherEnabled -> stringResource(R.string.weather_enable)
                weather == null -> "—"
                else -> "${weather.temperatureText(fahrenheit)} ${stringResource(weather.condition)}"
            }.uppercase(locale)
            BasicText(
                text = weatherText,
                style = LauncherType.data.copy(color = colors.onMesh),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onWeatherClick)
                    .padding(vertical = 14.dp),
            )
        }
    }
}

/**
 * Favoritas del inicio. Tocar abre la app; mantener presionado y arrastrar reordena;
 * mantener presionado y soltar sin mover muestra las acciones.
 */
@Composable
fun FavoritesList(
    favorites: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onMoveDone: () -> Unit,
    onRemove: (AppEntry) -> Unit,
    onInfo: (AppEntry) -> Unit,
) {
    val colors = LocalLauncherColors.current
    val haptics = LocalHapticFeedback.current
    val rowPx = with(LocalDensity.current) { FavoriteRowHeight.toPx() }
    val scope = rememberCoroutineScope()
    // El gesto vive más que una recomposición: siempre leer la lista actual
    val currentFavorites by rememberUpdatedState(favorites)

    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var actionsKey by remember { mutableStateOf<String?>(null) }

    if (favorites.isEmpty()) {
        BasicText(
            text = stringResource(R.string.favorites_empty),
            style = LauncherType.body.copy(color = colors.onMesh),
            modifier = Modifier.padding(end = 48.dp),
        )
        return
    }

    Column {
        favorites.forEach { app -> key(app.key) {
            // key: al reordenar, la fila (y su gesto en curso) se mueve con la app
            val dragging = draggingKey == app.key
            Column(
                modifier = Modifier
                    .zIndex(if (dragging) 1f else 0f)
                    .graphicsLayer {
                        if (dragging) {
                            translationY = dragOffset
                            scaleX = 1.04f
                            scaleY = 1.04f
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                        }
                    },
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FavoriteRowHeight)
                        .semantics(mergeDescendants = true) {
                            onClick(label = app.label) { onLaunch(app); true }
                            onLongClick { actionsKey = app.key; true }
                        }
                        .pointerInput(app.key) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val longPress = awaitLongPressOrCancellation(down.id)
                                if (longPress == null) {
                                    // Sin long press: es un toque si el dedo se levantó sin desplazarse
                                    val up = currentEvent.changes.firstOrNull { it.id == down.id }
                                    if (up != null && up.changedToUp() && !up.isConsumed &&
                                        (up.position - down.position).getDistance() < viewConfiguration.touchSlop
                                    ) {
                                        actionsKey = null
                                        onLaunch(app)
                                    }
                                    return@awaitEachGesture
                                }
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                actionsKey = null
                                draggingKey = app.key
                                dragOffset = 0f
                                var travelled = 0f
                                drag(longPress.id) { change ->
                                    val dy = change.positionChange().y
                                    travelled += abs(dy)
                                    dragOffset += dy
                                    change.consume()
                                    val index = currentFavorites.indexOfFirst { it.key == app.key }
                                    if (dragOffset > rowPx / 2 && index < currentFavorites.lastIndex) {
                                        onMove(index, index + 1)
                                        dragOffset -= rowPx
                                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    } else if (dragOffset < -rowPx / 2 && index > 0) {
                                        onMove(index, index - 1)
                                        dragOffset += rowPx
                                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    }
                                }
                                if (travelled < viewConfiguration.touchSlop) actionsKey = app.key
                                val settleFrom = dragOffset
                                scope.launch {
                                    animate(settleFrom, 0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 700f)) { v, _ ->
                                        dragOffset = v
                                    }
                                    draggingKey = null
                                }
                                onMoveDone()
                            }
                        },
                ) {
                    BasicText(
                        text = app.label,
                        style = LauncherType.favorite.copy(color = colors.onMesh),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AnimatedVisibility(
                    visible = actionsKey == app.key,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp),
                    ) {
                        Pill(stringResource(R.string.remove_from_home), onClick = {
                            actionsKey = null
                            onRemove(app)
                        })
                        Pill(
                            stringResource(R.string.app_info),
                            onClick = {
                                actionsKey = null
                                onInfo(app)
                            },
                            filled = false,
                            contentColor = colors.onMesh,
                        )
                    }
                }
            }
        } }
    }
}

@Composable
fun AppsPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Pill(stringResource(R.string.apps), onClick = onClick)
    }
}

/** Avance de 0 (cerrado) a 1 (abierto) con la física del resorte; respeta "reducir animaciones". */
suspend fun settleDrawer(
    from: Float,
    open: Boolean,
    velocity: Float,
    reduceMotion: Boolean,
    onValue: (Float) -> Unit,
) {
    val target = if (open) 1f else 0f
    if (reduceMotion) {
        onValue(target)
        return
    }
    animate(
        initialValue = from,
        targetValue = target,
        initialVelocity = velocity,
        animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f),
    ) { v, _ -> onValue(v) }
}
