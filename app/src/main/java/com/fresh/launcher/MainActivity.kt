package com.fresh.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.fresh.launcher.ui.AppDrawer
import com.fresh.launcher.ui.AppsPill
import com.fresh.launcher.ui.ClockBlock
import com.fresh.launcher.ui.DebugOverrides
import com.fresh.launcher.ui.FavoritesList
import com.fresh.launcher.ui.LauncherTheme
import com.fresh.launcher.ui.LocalLauncherColors
import com.fresh.launcher.ui.LocalReduceMotion
import com.fresh.launcher.ui.MeshWallpaperStyle
import com.fresh.launcher.ui.MeshWallpapers
import com.fresh.launcher.ui.WallpaperApplier
import com.fresh.launcher.ui.meshWallpaper
import com.fresh.launcher.ui.SettingsScreen
import com.fresh.launcher.ui.settleDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    // Cuenta las pulsaciones del botón de inicio mientras el launcher ya está abierto
    private val homePresses = mutableIntStateOf(0)
    private val debugOverrides = mutableStateOf(DebugOverrides())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readDebugOverrides(intent)
        setContent {
            LauncherTheme(debugOverrides.value) { Launcher(homePresses.intValue) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homePresses.intValue++
        readDebugOverrides(intent)
    }

    /**
     * Solo en debug: probar tema y tamaño de letra sin cambiar los ajustes del teléfono.
     * adb shell am start -n com.fresh.launcher/.MainActivity --es theme light --ef font_scale 1.3
     */
    private fun readDebugOverrides(intent: Intent) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        if (!intent.hasExtra("theme") && !intent.hasExtra("font_scale")) return
        debugOverrides.value = DebugOverrides(
            dark = when (intent.getStringExtra("theme")) {
                "light" -> false
                "dark" -> true
                else -> null
            },
            fontScale = intent.getFloatExtra("font_scale", 0f).takeIf { it > 0f },
        )
    }
}

@Composable
private fun Launcher(homePresses: Int) {
    val context = LocalContext.current
    val colors = LocalLauncherColors.current
    val reduceMotion = LocalReduceMotion.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val flingThreshold = with(LocalDensity.current) { 400.dp.toPx() }

    var apps by remember { mutableStateOf(emptyList<AppEntry>()) }
    val appsByKey = remember(apps) { apps.associateBy { it.key } }

    val favoritesStore = remember { FavoritesStore(context) }
    val favoriteKeys = remember { mutableStateListOf<String>().apply { addAll(favoritesStore.load()) } }
    val favoriteApps = favoriteKeys.mapNotNull { appsByKey[it] }

    val settingsStore = remember { SettingsStore(context) }
    var settings by remember { mutableStateOf(settingsStore.load()) }
    // null cuando el sistema no ofrece idioma por app (Android 12 o anterior)
    var language by remember { mutableStateOf(if (AppLanguages.supported) AppLanguages.current(context) else null) }
    // Saveable: cambiar el idioma recrea la actividad y la configuración debe seguir abierta
    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    val weatherRepo = remember { WeatherRepository(context) }
    var weather by remember { mutableStateOf(weatherRepo.cached()) }
    var weatherEnabled by remember { mutableStateOf(hasLocationPermission(context)) }
    val refreshWeather = { scope.launch { weatherRepo.refresh()?.let { weather = it } } }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        weatherEnabled = granted
        if (granted) refreshWeather()
    }

    fun selectWallpaper(style: MeshWallpaperStyle) {
        settings = settings.copy(wallpaperId = style.id, wallpaperChangedAt = System.currentTimeMillis())
        settingsStore.save(settings)
        val isDark = colors.isDark
        scope.launch(Dispatchers.Default) {
            runCatching { WallpaperApplier.apply(context, style, isDark, force = true) }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        // Cambio automático: se revisa al volver al inicio, sin trabajo en segundo plano
        if (settings.autoWallpaper &&
            System.currentTimeMillis() - settings.wallpaperChangedAt >= settings.autoWallpaperDays * DAY_MS
        ) {
            val index = MeshWallpapers.indexOfFirst { it.id == settings.wallpaperId }
            selectWallpaper(MeshWallpapers[(index + 1) % MeshWallpapers.size])
        }
        // Fuera del hilo principal: con 100+ apps, leer nombres bloqueaba el primer cuadro al volver al inicio
        scope.launch {
            val loaded = withContext(Dispatchers.Default) { loadLaunchableApps(context) }
            if (loaded != apps) apps = loaded
            // Quita de favoritas las apps desinstaladas, así los índices del reordenamiento coinciden
            val installed = loaded.mapTo(HashSet()) { it.key }
            if (favoriteKeys.retainAll { it in installed }) favoritesStore.save(favoriteKeys.toList())
        }
        weatherEnabled = hasLocationPermission(context)
        if (weatherEnabled && weatherRepo.isStale(weather)) refreshWeather()
    }

    // El fondo elegido es el fondo de pantalla del sistema (inicio y bloqueo); al cambiar el tema
    // se regenera su variante clara u oscura, solo si el fondo puesto sigue siendo nuestro
    LaunchedEffect(colors.isDark) {
        withContext(Dispatchers.Default) {
            runCatching { WallpaperApplier.apply(context, meshWallpaper(settings.wallpaperId), colors.isDark, force = false) }
        }
    }

    var query by remember { mutableStateOf("") }
    var progress by rememberSaveable { mutableFloatStateOf(0f) }

    // Entrada al volver al inicio: Android no les da a los launchers de terceros la animación del sistema,
    // así que el contenido llega con un zoom hacia adentro (0 → 1)
    // Arranca en 0,35 y no en 0: el sistema ya hace aparecer la ventana de a poco, y sumar
    // nuestro fundido desde cero dejaba ~170 ms de fondo vacío
    val arrivalStart = 0.35f
    val arrival = remember { Animatable(if (reduceMotion) 1f else arrivalStart) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        // Se deja listo al ocultarse, así al volver no se ve ni un cuadro del estado final
        if (!reduceMotion) scope.launch { arrival.snapTo(arrivalStart) }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        scope.launch { arrival.animateTo(1f, spring(dampingRatio = 0.9f, stiffness = 320f)) }
    }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val searchFocus = remember { FocusRequester() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val heightPx = constraints.maxHeight.toFloat()

        fun settle(open: Boolean, velocityPx: Float) {
            settleJob?.cancel()
            if (!open) {
                keyboard?.hide()
                focusManager.clearFocus()
            }
            settleJob = scope.launch {
                settleDrawer(progress, open, -velocityPx / heightPx, reduceMotion) { progress = it }
                if (open && settings.autoKeyboard) {
                    searchFocus.requestFocus()
                    keyboard?.show()
                } else {
                    query = ""
                }
            }
        }

        fun settleByVelocity(velocityPx: Float) = settle(
            open = if (abs(velocityPx) > flingThreshold) velocityPx < 0 else progress > 0.5f,
            velocityPx = velocityPx,
        )

        fun dragBy(deltaPx: Float) {
            settleJob?.cancel()
            if (deltaPx > 0) keyboard?.hide()
            progress = (progress - deltaPx / heightPx).coerceIn(0f, 1f)
        }

        val dragState = rememberDraggableState { dragBy(it) }

        // Con la lista arriba de todo, seguir arrastrando hacia abajo cierra el cajón
        val drawerConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput && progress < 1f && available.y < 0f) {
                        dragBy(available.y)
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput && available.y > 0f) {
                        dragBy(available.y)
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (progress < 1f) {
                        settleByVelocity(available.y)
                        return available
                    }
                    return Velocity.Zero
                }
            }
        }

        BackHandler {
            when {
                settingsOpen -> settingsOpen = false
                progress > 0f -> settle(open = false, velocityPx = 0f)
            }
        }
        LaunchedEffect(homePresses) {
            if (homePresses > 0) {
                settingsOpen = false
                if (progress > 0f) settle(open = false, velocityPx = 0f)
            }
        }

        fun launchAndReset(app: AppEntry) {
            launchApp(context, app)
            keyboard?.hide()
            focusManager.clearFocus()
            scope.launch {
                // Esperar a que la animación de apertura de la app tape el cajón
                delay(350)
                settleJob?.cancel()
                progress = 0f
                query = ""
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // El inicio retrocede mientras sube el cajón
                    val p = progress
                    val a = arrival.value
                    // La opacidad llega antes que la escala para que no haya cuadros vacíos
                    alpha = (1f - p * 0.7f) * (a * 1.8f).coerceAtMost(1f)
                    val s = (1f - p * 0.05f) * (1.08f - 0.08f * a)
                    scaleX = s
                    scaleY = s
                    translationY = -p * heightPx * 0.04f
                }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { settleByVelocity(it) },
                )
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
        ) {
            Spacer(Modifier.height(40.dp))
            // Los minutos del reloj se tiñen con el tono del fondo elegido
            val mutedForWallpaper = meshWallpaper(settings.wallpaperId).palette(colors.isDark).muted
            CompositionLocalProvider(LocalLauncherColors provides colors.copy(onMeshMuted = mutedForWallpaper)) {
                ClockBlock(
                    weather = weather,
                    fahrenheit = settings.fahrenheit,
                    weatherEnabled = weatherEnabled,
                    onWeatherClick = {
                        if (weatherEnabled) refreshWeather()
                        else locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    },
                )
            }
            Spacer(Modifier.weight(1f))
            FavoritesList(
                favorites = favoriteApps,
                onLaunch = { launchApp(context, it) },
                onMove = { from, to -> favoriteKeys.add(to, favoriteKeys.removeAt(from)) },
                onMoveDone = { favoritesStore.save(favoriteKeys.toList()) },
                onRemove = {
                    favoriteKeys.remove(it.key)
                    favoritesStore.save(favoriteKeys.toList())
                },
                onInfo = { openAppInfo(context, it) },
            )
            Spacer(Modifier.height(28.dp))
            AppsPill(onClick = { settle(open = true, velocityPx = 0f) })
            Spacer(Modifier.height(16.dp))
        }

        AppDrawer(
            apps = apps,
            query = query,
            onQueryChange = { query = it },
            favoriteKeys = favoriteKeys,
            searchFocus = searchFocus,
            dragConnection = drawerConnection,
            onLaunch = ::launchAndReset,
            onToggleFavorite = {
                if (it.key in favoriteKeys) favoriteKeys.remove(it.key)
                else if (favoriteKeys.size < FavoritesStore.MAX) favoriteKeys.add(it.key)
                favoritesStore.save(favoriteKeys.toList())
            },
            onInfo = { openAppInfo(context, it) },
            onOpenSettings = {
                keyboard?.hide()
                focusManager.clearFocus()
                settingsOpen = true
            },
            modifier = Modifier
                .graphicsLayer { translationY = (1f - progress) * heightPx }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { settleByVelocity(it) },
                ),
        )

        AnimatedVisibility(
            visible = settingsOpen,
            enter = if (reduceMotion) fadeIn() else slideInHorizontally { it } + fadeIn(),
            exit = if (reduceMotion) fadeOut() else slideOutHorizontally { it } + fadeOut(),
        ) {
            SettingsScreen(
                settings = settings,
                onSettingsChange = {
                    settings = it
                    settingsStore.save(it)
                },
                language = language,
                onLanguageChange = {
                    language = it
                    AppLanguages.set(context, it)
                },
                onSelectWallpaper = { selectWallpaper(it) },
                onBack = { settingsOpen = false },
            )
        }
    }
}

private const val DAY_MS = 24 * 60 * 60 * 1000L
