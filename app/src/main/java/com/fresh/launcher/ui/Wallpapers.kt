package com.fresh.launcher.ui

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.fresh.launcher.R
import kotlin.random.Random

/** Mancha de color: posición relativa a la pantalla y radio relativo a la altura. */
class Blob(val color: Color, val x: Float, val y: Float, val radius: Float)

/**
 * Una variante (clara u oscura) de un fondo. En claro la base es luminosa para el texto negro;
 * en oscuro es negro puro por el AMOLED. [muted] tiñe los minutos del reloj con el tono del fondo.
 */
class MeshPalette(val base: Color, val blobs: List<Blob>, val muted: Color)

class MeshWallpaperStyle(
    val id: String,
    @StringRes val name: Int,
    val light: MeshPalette,
    val dark: MeshPalette,
) {
    fun palette(isDark: Boolean) = if (isDark) dark else light
}

// Mismo lenguaje (manchas difusas + grano), distinto patrón y color en cada uno
val MeshWallpapers = listOf(
    // Fresh: el original, manchas en las esquinas
    MeshWallpaperStyle(
        id = "fresh",
        name = R.string.wallpaper_fresh,
        light = MeshPalette(
            base = FreshGreen,
            blobs = listOf(
                Blob(FreshLime, 0.95f, 0.08f, 0.45f),
                Blob(Color(0xFF00C417), 0.35f, 0.42f, 0.38f),
                Blob(Color(0xFFE4F2C8), 0.05f, 0.95f, 0.42f),
                Blob(FreshLime.copy(alpha = 0.8f), 0.9f, 0.78f, 0.3f),
            ),
            muted = Leaf.copy(alpha = 0.55f),
        ),
        dark = MeshPalette(
            base = Color.Black,
            blobs = listOf(
                Blob(Leaf, 0.15f, 0.55f, 0.55f),
                Blob(FreshGreen.copy(alpha = 0.45f), 1.0f, 1.0f, 0.5f),
                Blob(FreshLime.copy(alpha = 0.16f), 0.95f, 0.05f, 0.35f),
            ),
            muted = Color(0xFF8FB894),
        ),
    ),
    // Cítrico: banda diagonal de verde a amarillo limón
    MeshWallpaperStyle(
        id = "citrus",
        name = R.string.wallpaper_citrus,
        light = MeshPalette(
            base = FreshLime,
            blobs = listOf(
                Blob(FreshGreen.copy(alpha = 0.85f), 0.0f, 0.0f, 0.5f),
                Blob(Color(0xFF9BE800), 0.6f, 0.32f, 0.3f),
                Blob(Color(0xFFFFF27A), 1.0f, 0.58f, 0.45f),
                Blob(Color(0xFFF6FFD6), 0.25f, 1.0f, 0.42f),
            ),
            muted = Leaf.copy(alpha = 0.55f),
        ),
        dark = MeshPalette(
            base = Color.Black,
            blobs = listOf(
                Blob(FreshLime.copy(alpha = 0.3f), 0.0f, 0.0f, 0.55f),
                Blob(Color(0xFF3A5200), 1.0f, 0.6f, 0.5f),
                Blob(FreshGreen.copy(alpha = 0.18f), 0.4f, 1.0f, 0.4f),
            ),
            muted = Color(0xFFC8DB8A),
        ),
    ),
    // Menta: un orbe lima en el centro
    MeshWallpaperStyle(
        id = "mint",
        name = R.string.wallpaper_mint,
        light = MeshPalette(
            base = Color(0xFF7DF2C4),
            blobs = listOf(
                Blob(Color(0xFF00C9A7), 0.0f, 0.15f, 0.45f),
                Blob(Color(0xFF3EE08F), 1.0f, 0.2f, 0.3f),
                Blob(Color(0xFFE8FFF6), 1.0f, 0.95f, 0.45f),
                Blob(FreshLime.copy(alpha = 0.7f), 0.5f, 0.52f, 0.33f),
            ),
            muted = Color(0xFF00523F).copy(alpha = 0.6f),
        ),
        dark = MeshPalette(
            base = Color.Black,
            blobs = listOf(
                Blob(Color(0xFF0E3B33), 0.0f, 0.0f, 0.5f),
                Blob(Color(0xFF00C9A7).copy(alpha = 0.4f), 0.5f, 0.55f, 0.45f),
                Blob(FreshLime.copy(alpha = 0.12f), 1.0f, 1.0f, 0.35f),
            ),
            muted = Color(0xFF8CC9B8),
        ),
    ),
    // Cielo: azul arriba y horizonte lima abajo
    MeshWallpaperStyle(
        id = "sky",
        name = R.string.wallpaper_sky,
        light = MeshPalette(
            base = Color(0xFF8FD3FF),
            blobs = listOf(
                Blob(Color(0xFF4DA8FF), 0.0f, 0.0f, 0.5f),
                Blob(Color(0xFFE9F7FF), 1.0f, 0.35f, 0.35f),
                Blob(Color(0xFFB6F0FF), 0.2f, 0.6f, 0.3f),
                Blob(FreshLime.copy(alpha = 0.75f), 0.5f, 1.05f, 0.45f),
            ),
            muted = Color(0xFF0B3A66).copy(alpha = 0.55f),
        ),
        dark = MeshPalette(
            base = Color.Black,
            blobs = listOf(
                Blob(Color(0xFF1E5BFF).copy(alpha = 0.35f), 0.0f, 0.0f, 0.55f),
                Blob(Color(0xFF0A1F44), 0.8f, 0.5f, 0.5f),
                Blob(FreshLime.copy(alpha = 0.14f), 0.5f, 1.05f, 0.4f),
            ),
            muted = Color(0xFF93B4E0),
        ),
    ),
    // Lila: brillos en esquinas opuestas
    MeshWallpaperStyle(
        id = "lilac",
        name = R.string.wallpaper_lilac,
        light = MeshPalette(
            base = Color(0xFFCDB8FF),
            blobs = listOf(
                Blob(Color(0xFFA98BFF), 0.0f, 0.2f, 0.35f),
                Blob(Color(0xFFFFB5E8), 1.0f, 0.0f, 0.45f),
                Blob(Color(0xFFEFE8FF), 0.6f, 0.55f, 0.35f),
                Blob(FreshLime.copy(alpha = 0.7f), 0.0f, 1.0f, 0.45f),
            ),
            muted = Color(0xFF3B1F73).copy(alpha = 0.55f),
        ),
        dark = MeshPalette(
            base = Color.Black,
            blobs = listOf(
                Blob(Color(0xFF6B3BFF).copy(alpha = 0.38f), 1.0f, 0.0f, 0.55f),
                Blob(Color(0xFF2A1450), 0.1f, 0.6f, 0.5f),
                Blob(FreshLime.copy(alpha = 0.12f), 0.0f, 1.0f, 0.4f),
            ),
            muted = Color(0xFFB7A6E6),
        ),
    ),
    // Coral: sol amarillo arriba a la izquierda sobre coral
    MeshWallpaperStyle(
        id = "coral",
        name = R.string.wallpaper_coral,
        light = MeshPalette(
            base = Color(0xFFFF9A76),
            blobs = listOf(
                Blob(Color(0xFFFFD36E), 0.0f, 0.0f, 0.5f),
                Blob(Color(0xFFFF6B6B).copy(alpha = 0.8f), 1.0f, 0.6f, 0.45f),
                Blob(Color(0xFFFFE9DC), 0.3f, 1.0f, 0.4f),
                Blob(FreshLime.copy(alpha = 0.45f), 0.9f, 0.05f, 0.25f),
            ),
            muted = Color(0xFF6B1F0F).copy(alpha = 0.55f),
        ),
        dark = MeshPalette(
            base = Color.Black,
            blobs = listOf(
                Blob(Color(0xFFFFD36E).copy(alpha = 0.14f), 0.0f, 0.0f, 0.35f),
                Blob(Color(0xFF4A1508), 0.0f, 0.3f, 0.5f),
                Blob(Color(0xFFFF5A3C).copy(alpha = 0.35f), 1.0f, 0.7f, 0.55f),
            ),
            muted = Color(0xFFE3A99A),
        ),
    ),
    // Terracota: luz marfil arriba a la derecha y fondo profundo abajo a la izquierda
    MeshWallpaperStyle(
        id = "terracotta",
        name = R.string.wallpaper_terracotta,
        light = MeshPalette(
            base = Color(0xFFD97757),
            blobs = listOf(
                Blob(Color(0xFFC15F3C), 0.0f, 0.72f, 0.5f),
                Blob(Color(0xFFE89A78), 0.5f, 0.45f, 0.3f),
                Blob(Color(0xFFF5C8A8).copy(alpha = 0.8f), 0.2f, 0.05f, 0.3f),
                Blob(Color(0xFFF0EEE6), 0.88f, 0.15f, 0.42f),
            ),
            muted = Color(0xFF5A2312).copy(alpha = 0.6f),
        ),
        dark = MeshPalette(
            base = Color.Black,
            blobs = listOf(
                Blob(Color(0xFFF0EEE6).copy(alpha = 0.08f), 0.0f, 0.0f, 0.35f),
                Blob(Color(0xFF8A3A1F).copy(alpha = 0.35f), 0.1f, 0.85f, 0.5f),
                Blob(Color(0xFFD97757).copy(alpha = 0.55f), 0.9f, 0.25f, 0.5f),
            ),
            muted = Color(0xFFE3A48A),
        ),
    ),
)

fun meshWallpaper(id: String): MeshWallpaperStyle = MeshWallpapers.firstOrNull { it.id == id } ?: MeshWallpapers.first()

private fun DrawScope.drawMesh(palette: MeshPalette, isDark: Boolean) {
    drawRect(palette.base)
    palette.blobs.forEach { drawBlob(it) }
    // Overlay: el gris medio del ruido no apaga la saturación del color
    drawRect(Grain, alpha = if (isDark) 0.08f else 0.14f, blendMode = BlendMode.Overlay)
}

private fun DrawScope.drawBlob(blob: Blob) {
    val center = Offset(blob.x * size.width, blob.y * size.height)
    val radius = blob.radius * size.height
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to blob.color,
                0.55f to blob.color.copy(alpha = blob.color.alpha * 0.45f),
                1f to blob.color.copy(alpha = 0f),
            ),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

/** Textura de grano monocroma de 128×128, generada una sola vez y compartida. */
private val Grain: ShaderBrush by lazy {
    val size = 128
    val random = Random(7)
    val pixels = IntArray(size * size) {
        val v = random.nextInt(256)
        (0xFF shl 24) or (v shl 16) or (v shl 8) or v
    }
    val image: ImageBitmap = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
    ShaderBrush(ImageShader(image, TileMode.Repeated, TileMode.Repeated))
}

/** Miniatura del fondo con un reloj de muestra, para ver cómo queda el texto encima. */
@Composable
fun WallpaperPreview(style: MeshWallpaperStyle, isDark: Boolean, modifier: Modifier = Modifier) {
    val palette = style.palette(isDark)
    val textColor = if (isDark) Bone else Ink
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) { drawMesh(palette, isDark) }
        BasicText(
            text = buildAnnotatedString {
                append("09")
                withStyle(SpanStyle(color = palette.muted)) { append(":41") }
            },
            style = LauncherType.clock.copy(color = textColor, fontSize = 20.sp, lineHeight = 20.sp),
            modifier = Modifier.padding(start = 8.dp, top = 18.dp),
        )
    }
}

private fun renderMesh(palette: MeshPalette, isDark: Boolean, width: Int, height: Int): Bitmap {
    val image = ImageBitmap(width, height)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(width.toFloat(), height.toFloat())) {
        drawMesh(palette, isDark)
    }
    return image.asAndroidBitmap()
}

/**
 * El fondo vive como fondo de pantalla del sistema, de inicio y de bloqueo: así Android usa su
 * transición de "volver al inicio" sin mostrar otro fondo de por medio.
 * Sin [force] (cambio de tema) solo se reemplaza un fondo que pusimos nosotros; con [force]
 * (el usuario eligió uno, o tocó el cambio automático) se aplica siempre.
 */
object WallpaperApplier {
    // Subir cuando cambie el diseño de los fondos o cómo se aplican, para regenerarlos.
    // v2: también en la pantalla de bloqueo.
    private const val VERSION = 2

    @Synchronized
    fun apply(context: Context, style: MeshWallpaperStyle, isDark: Boolean, force: Boolean) {
        val manager = WallpaperManager.getInstance(context)
        if (!manager.isWallpaperSupported || !manager.isSetWallpaperAllowed) return
        val prefs = context.getSharedPreferences("wallpaper", Context.MODE_PRIVATE)

        val variant = "${style.id}-${if (isDark) "dark" else "light"}-$VERSION"
        val ours = prefs.getInt("id", Int.MIN_VALUE) == manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)
        val firstRun = !prefs.contains("variant")
        if (!force && !firstRun && !ours) return
        if (ours && prefs.getString("variant", null) == variant) return

        val (width, height) = screenSize(context)
        val bitmap = renderMesh(style.palette(isDark), isDark, width, height)
        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
        bitmap.recycle()
        prefs.edit {
            putString("variant", variant)
            putInt("id", manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM))
        }
    }

    /** Tamaño físico de la pantalla, incluidas las barras del sistema. */
    private fun screenSize(context: Context): Pair<Int, Int> {
        val windowManager = context.getSystemService(WindowManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.maximumWindowMetrics.bounds
            return bounds.width() to bounds.height()
        }
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        return metrics.widthPixels to metrics.heightPixels
    }
}
