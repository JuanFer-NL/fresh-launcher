package com.fresh.launcher.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.fresh.launcher.AppLanguage
import com.fresh.launcher.LauncherSettings
import com.fresh.launcher.R

@Composable
fun SettingsScreen(
    settings: LauncherSettings,
    onSettingsChange: (LauncherSettings) -> Unit,
    language: AppLanguage?,
    onLanguageChange: (AppLanguage) -> Unit,
    onSelectWallpaper: (MeshWallpaperStyle) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLauncherColors.current

    Column(
        modifier
            .fillMaxSize()
            .background(colors.drawer)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        ) {
            val backLabel = stringResource(R.string.back)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(PillShape)
                    .clickable(onClickLabel = backLabel, role = Role.Button, onClick = onBack),
            ) {
                BackIcon(colors.onDrawer)
            }
            BasicText(
                text = stringResource(R.string.settings),
                style = LauncherType.favorite.copy(color = colors.onDrawer, fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        WallpaperSection(settings, onSettingsChange, onSelectWallpaper)
        Divider()

        // null = el sistema no soporta idioma por app (Android 12 o anterior)
        if (language != null) {
            Section(stringResource(R.string.language)) {
                Choices(
                    options = listOf(
                        AppLanguage.SYSTEM to stringResource(R.string.language_system),
                        // Cada idioma con su propio nombre, para encontrarlo aunque no se entienda el actual
                        AppLanguage.ENGLISH to "English",
                        AppLanguage.SPANISH to "Español",
                    ),
                    selected = language,
                    onSelect = onLanguageChange,
                )
            }
            Divider()
        }

        Section(stringResource(R.string.temperature_unit)) {
            Choices(
                options = listOf(
                    false to stringResource(R.string.celsius),
                    true to stringResource(R.string.fahrenheit),
                ),
                selected = settings.fahrenheit,
                onSelect = { onSettingsChange(settings.copy(fahrenheit = it)) },
            )
        }
        Divider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(16.dp))
                .toggleable(
                    value = settings.autoKeyboard,
                    role = Role.Switch,
                    onValueChange = { onSettingsChange(settings.copy(autoKeyboard = it)) },
                )
                .padding(horizontal = 8.dp, vertical = 16.dp),
        ) {
            Column(Modifier.weight(1f)) {
                BasicText(stringResource(R.string.auto_keyboard), style = SectionTitle.copy(color = colors.onDrawer))
                BasicText(
                    stringResource(R.string.auto_keyboard_desc),
                    style = LauncherType.body.copy(color = colors.onDrawerMuted, fontSize = 14.sp, lineHeight = 20.sp),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Toggle(checked = settings.autoKeyboard, onCheckedChange = null)
        }
        Divider()

        // Atribución que pide la licencia CC BY 4.0 de los datos de Open-Meteo
        val uriHandler = LocalUriHandler.current
        BasicText(
            text = stringResource(R.string.weather_attribution),
            style = LauncherType.data.copy(color = colors.onDrawerMuted),
            modifier = Modifier
                .clip(PillShape)
                .clickable(role = Role.Button) { uriHandler.openUri("https://open-meteo.com/") }
                .padding(horizontal = 8.dp, vertical = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
    }
}

private val SectionTitle = LauncherType.app.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em)

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val colors = LocalLauncherColors.current
    Column(Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
        BasicText(title, style = SectionTitle.copy(color = colors.onDrawer))
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalLauncherColors.current.onDrawer.copy(alpha = 0.08f)),
    )
}

/** Opciones excluyentes como pills: la elegida rellena, el resto con contorno. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Choices(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Pill(
                text = label,
                filled = isSelected,
                onClick = { if (!isSelected) onSelect(value) },
                modifier = Modifier.semantics { this.selected = isSelected },
            )
        }
    }
}

/**
 * Interruptor de la app: pista negra (o lima en oscuro) con perilla del color de acento.
 * Con [onCheckedChange] null lo maneja la fila que lo contiene.
 */
@Composable
private fun Toggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    val colors = LocalLauncherColors.current
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "toggle",
    )
    val touch = if (onCheckedChange != null) {
        // 52×32 visible, con área táctil de 48 dp de alto
        Modifier
            .clip(PillShape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 8.dp)
    } else {
        Modifier
    }
    Box(
        touch
            .size(width = 52.dp, height = 32.dp)
            .clip(PillShape)
            .background(if (checked) colors.pill else colors.onDrawer.copy(alpha = 0.14f)),
    ) {
        Box(
            Modifier
                .offset { IntOffset(knobOffset.roundToPx(), 4.dp.roundToPx()) }
                .size(24.dp)
                .clip(PillShape)
                .background(if (checked) colors.onPill else colors.onDrawer.copy(alpha = 0.7f)),
        )
    }
}

@Composable
private fun WallpaperSection(
    settings: LauncherSettings,
    onSettingsChange: (LauncherSettings) -> Unit,
    onSelectWallpaper: (MeshWallpaperStyle) -> Unit,
) {
    val colors = LocalLauncherColors.current
    Column(Modifier.padding(vertical = 16.dp)) {
        BasicText(
            stringResource(R.string.wallpaper),
            style = SectionTitle.copy(color = colors.onDrawer),
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        BasicText(
            stringResource(R.string.wallpaper_hint),
            style = LauncherType.body.copy(color = colors.onDrawerMuted, fontSize = 14.sp, lineHeight = 20.sp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp),
        )
        Spacer(Modifier.height(14.dp))

        // Las miniaturas se desplazan de costado; los 8 dp extra dejan ver el anillo de la elegida
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            items(MeshWallpapers, key = { it.id }) { style ->
                val selected = style.id == settings.wallpaperId
                val name = stringResource(style.name)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelectWallpaper(style) })
                        // Sin etiqueta visible: el nombre queda solo para TalkBack
                        .semantics { contentDescription = name }
                        .padding(4.dp),
                ) {
                    WallpaperPreview(
                        style = style,
                        isDark = colors.isDark,
                        modifier = Modifier
                            .border(
                                width = 2.dp,
                                color = if (selected) colors.onDrawer else Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(4.dp)
                            .size(width = 72.dp, height = 156.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        AutoWallpaperRow(settings, onSettingsChange)
    }
}

/** "Cambiar automáticamente cada [−] X [+] días" con su interruptor. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AutoWallpaperRow(settings: LauncherSettings, onSettingsChange: (LauncherSettings) -> Unit) {
    val colors = LocalLauncherColors.current
    val days = settings.autoWallpaperDays
    val textStyle = SectionTitle.copy(color = colors.onDrawer, fontSize = 16.sp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        FlowRow(
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            // Cuando el texto ocupa su propia línea, el selector no queda pegado debajo
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f),
        ) {
            BasicText(stringResource(R.string.auto_wallpaper_prefix), style = textStyle)
            DayStepper(
                days = days,
                onChange = { onSettingsChange(settings.copy(autoWallpaperDays = it)) },
            )
            BasicText(pluralStringResource(R.plurals.auto_wallpaper_days, days), style = textStyle)
        }
        Spacer(Modifier.size(12.dp))
        Toggle(
            checked = settings.autoWallpaper,
            onCheckedChange = { enabled ->
                // Al activarlo, el plazo empieza a contar desde ahora
                onSettingsChange(
                    settings.copy(
                        autoWallpaper = enabled,
                        wallpaperChangedAt = if (enabled) System.currentTimeMillis() else settings.wallpaperChangedAt,
                    ),
                )
            },
        )
    }
}

@Composable
private fun DayStepper(days: Int, onChange: (Int) -> Unit) {
    val colors = LocalLauncherColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(PillShape)
            .background(colors.field),
    ) {
        StepButton("−", stringResource(R.string.fewer_days), enabled = days > LauncherSettings.AutoDaysRange.first) {
            onChange(days - 1)
        }
        BasicText(
            text = days.toString(),
            // Mono: es un dato, y el ancho fijo evita que el control salte de 9 a 10
            style = LauncherType.data.copy(color = colors.onDrawer, fontSize = 16.sp, letterSpacing = 0.em, textAlign = TextAlign.Center),
            modifier = Modifier.width(26.dp),
        )
        StepButton("+", stringResource(R.string.more_days), enabled = days < LauncherSettings.AutoDaysRange.last) {
            onChange(days + 1)
        }
    }
}

@Composable
private fun StepButton(symbol: String, label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalLauncherColors.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(PillShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
    ) {
        BasicText(
            symbol,
            style = LauncherType.app.copy(color = colors.onDrawer.copy(alpha = if (enabled) 1f else 0.3f)),
        )
    }
}
