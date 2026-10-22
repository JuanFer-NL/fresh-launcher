package com.fresh.launcher.ui

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.fresh.launcher.R

// Paleta de Fresh Launcher (DESIGN.md §3)
val FreshGreen = Color(0xFF00DF1A)
val FreshLime = Color(0xFFCFFF2E)
val Ink = Color(0xFF121212)
val Leaf = Color(0xFF003D1B)
val Bone = Color(0xFFF3F2EE)

@Immutable
data class LauncherColors(
    val isDark: Boolean,
    /** Texto sobre el degradé del inicio. */
    val onMesh: Color,
    /** Segundo tono del reloj y textos secundarios sobre el degradé, teñido de verde. */
    val onMeshMuted: Color,
    /** Fondo del cajón de apps. */
    val drawer: Color,
    val onDrawer: Color,
    val onDrawerMuted: Color,
    val field: Color,
    /** Pill principal: negro con texto lima en claro, lima con texto hoja en oscuro. */
    val pill: Color,
    val onPill: Color,
)

private val Light = LauncherColors(
    isDark = false,
    onMesh = Ink,
    onMeshMuted = Leaf.copy(alpha = 0.55f),
    drawer = Bone,
    onDrawer = Ink,
    onDrawerMuted = Color(0xFF6B6B66),
    field = Color.White,
    pill = Ink,
    onPill = FreshLime,
)

// Negro puro: en pantallas AMOLED los píxeles negros se apagan y ahorran batería
private val Dark = LauncherColors(
    isDark = true,
    onMesh = Bone,
    onMeshMuted = Color(0xFF8FB894),
    drawer = Color.Black,
    onDrawer = Bone,
    onDrawerMuted = Color(0xFF8A8A85),
    field = Color(0xFF161616),
    pill = FreshLime,
    onPill = Leaf,
)

@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: Int) = Font(
    resId = resId,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Geist = FontFamily(
    listOf(400, 500, 600, 700, 800, 900).map { variable(R.font.geist, it) },
)

val GeistMono = FontFamily(
    listOf(400, 500).map { variable(R.font.geist_mono, it) },
)

/** Escala tipográfica (DESIGN.md §3). En sp para respetar el tamaño de fuente del sistema. */
object LauncherType {
    val clock = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Black,
        fontSize = 96.sp,
        lineHeight = 88.sp,
        letterSpacing = (-0.04).em,
        // Sin el aire interno de la fuente: la fecha queda pegada al reloj
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
    )
    val favorite = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).em,
    )
    val app = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).em,
    )
    val body = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )
    val pill = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    )

    /** Solo para datos medidos: fecha, temperatura, letras del índice. */
    val data = TextStyle(
        fontFamily = GeistMono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.06.em,
    )
}

val LocalLauncherColors = staticCompositionLocalOf { Light }

/** true cuando el usuario desactivó las animaciones en Opciones de desarrollador o Accesibilidad. */
val LocalReduceMotion = staticCompositionLocalOf { false }

/** Forzados solo para pruebas en builds debug (ver MainActivity); null = seguir al sistema. */
data class DebugOverrides(val dark: Boolean? = null, val fontScale: Float? = null)

@Composable
fun LauncherTheme(overrides: DebugOverrides = DebugOverrides(), content: @Composable () -> Unit) {
    val colors = if (overrides.dark ?: isSystemInDarkTheme()) Dark else Light
    val context = LocalContext.current
    val density = LocalDensity.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val selection = TextSelectionColors(
        handleColor = if (colors.isDark) FreshLime else Ink,
        backgroundColor = FreshLime.copy(alpha = if (colors.isDark) 0.35f else 0.6f),
    )
    CompositionLocalProvider(
        LocalLauncherColors provides colors,
        LocalReduceMotion provides reduceMotion,
        LocalTextSelectionColors provides selection,
        LocalDensity provides Density(density.density, overrides.fontScale ?: density.fontScale),
        content = content,
    )
}
