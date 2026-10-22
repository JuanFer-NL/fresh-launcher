package com.fresh.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

val PillShape = RoundedCornerShape(percent = 50)

/** Botón pill de la app. [filled] = negro con lima; si no, contorno fino sobre el fondo. */
@Composable
fun Pill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    enabled: Boolean = true,
    contentColor: Color = LocalLauncherColors.current.onDrawer,
) {
    val colors = LocalLauncherColors.current
    val fg = if (filled) colors.onPill else contentColor
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(PillShape)
            .then(
                if (filled) Modifier.background(colors.pill)
                else Modifier.border(1.dp, contentColor.copy(alpha = 0.22f), PillShape),
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        BasicText(
            text = text,
            style = LauncherType.pill.copy(color = if (enabled) fg else fg.copy(alpha = 0.45f)),
            maxLines = 1,
        )
    }
}
