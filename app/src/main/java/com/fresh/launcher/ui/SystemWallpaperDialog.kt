package com.fresh.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fresh.launcher.R

/**
 * Pregunta si el degradé también se aplica como fondo de pantalla del sistema, reemplazando el del
 * usuario. Cerrarlo con "atrás" equivale a mantener el fondo propio: nunca se reemplaza sin un "sí".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemWallpaperDialog(onApply: () -> Unit, onKeep: () -> Unit) {
    val colors = LocalLauncherColors.current
    Dialog(
        onDismissRequest = onKeep,
        // Tocar afuera no cuenta como respuesta: reemplazar el fondo no se puede deshacer.
        // Ancho propio (no el del sistema) para que los dos botones entren lado a lado
        properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.drawer)
                .padding(24.dp),
        ) {
            BasicText(
                text = stringResource(R.string.system_wallpaper_title),
                style = LauncherType.favorite.copy(
                    color = colors.onDrawer,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
                modifier = Modifier.semantics { heading() },
            )
            BasicText(
                text = stringResource(R.string.system_wallpaper_body),
                style = LauncherType.body.copy(color = colors.onDrawerMuted),
            )
            // Lado a lado y centrados; con letra grande pasan a otra línea (centrados) en vez de cortarse
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Pill(stringResource(R.string.system_wallpaper_keep), onClick = onKeep, filled = false)
                Pill(stringResource(R.string.system_wallpaper_apply), onClick = onApply)
            }
        }
    }
}
