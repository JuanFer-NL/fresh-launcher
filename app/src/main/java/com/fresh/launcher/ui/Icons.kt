package com.fresh.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Íconos de línea en una grilla de 24, trazo de 1,6 (DESIGN.md §3: monocromos, un solo grosor)
private const val Grid = 24f
private const val StrokeWidth = 1.6f

private fun DrawScope.u(v: Float) = v * size.minDimension / Grid

/** Tres deslizadores: la configuración. */
@Composable
fun SettingsIcon(color: Color, background: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val stroke = u(StrokeWidth)
        listOf(6f to 15f, 12f to 8f, 18f to 16f).forEach { (y, knob) ->
            drawLine(color, Offset(u(3f), u(y)), Offset(u(21f), u(y)), stroke, StrokeCap.Round)
            // La perilla tapa la línea con el color del fondo y lleva su propio contorno
            drawCircle(background, u(2.6f), Offset(u(knob), u(y)))
            drawCircle(color, u(2.6f), Offset(u(knob), u(y)), style = Stroke(stroke))
        }
    }
}

/** Flecha hacia la izquierda: volver. */
@Composable
fun BackIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(u(11f), u(5f))
            lineTo(u(4f), u(12f))
            lineTo(u(11f), u(19f))
            moveTo(u(4.5f), u(12f))
            lineTo(u(20f), u(12f))
        }
        drawPath(path, color, style = Stroke(u(StrokeWidth), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
