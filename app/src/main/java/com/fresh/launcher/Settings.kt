package com.fresh.launcher

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit

data class LauncherSettings(
    val fahrenheit: Boolean = false,
    /** Abrir el teclado del buscador apenas termina de abrirse el cajón. */
    val autoKeyboard: Boolean = true,
    val wallpaperId: String = "fresh",
    /** Pasar al siguiente fondo cada [autoWallpaperDays] días (se revisa al volver al inicio). */
    val autoWallpaper: Boolean = false,
    val autoWallpaperDays: Int = 3,
    /** Cuándo cambió el fondo por última vez: el plazo del cambio automático cuenta desde acá. */
    val wallpaperChangedAt: Long = 0L,
) {
    companion object {
        val AutoDaysRange = 1..30
    }
}

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load() = LauncherSettings(
        fahrenheit = prefs.getBoolean("fahrenheit", false),
        autoKeyboard = prefs.getBoolean("auto_keyboard", true),
        wallpaperId = prefs.getString("wallpaper", null) ?: "fresh",
        autoWallpaper = prefs.getBoolean("auto_wallpaper", false),
        autoWallpaperDays = prefs.getInt("auto_wallpaper_days", 3).coerceIn(LauncherSettings.AutoDaysRange),
        wallpaperChangedAt = prefs.getLong("wallpaper_changed_at", 0L),
    )

    fun save(settings: LauncherSettings) {
        prefs.edit {
            putBoolean("fahrenheit", settings.fahrenheit)
            putBoolean("auto_keyboard", settings.autoKeyboard)
            putString("wallpaper", settings.wallpaperId)
            putBoolean("auto_wallpaper", settings.autoWallpaper)
            putInt("auto_wallpaper_days", settings.autoWallpaperDays)
            putLong("wallpaper_changed_at", settings.wallpaperChangedAt)
        }
    }
}

/** Idiomas que ofrece la app; deben coincidir con res/xml/locales_config.xml. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    SPANISH("es"),
}

/** Idioma por app de Android 13+: lo guarda el sistema y recrea la actividad al cambiarlo. */
object AppLanguages {
    val supported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun current(context: Context): AppLanguage {
        if (!supported) return AppLanguage.SYSTEM
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (locales.isEmpty) return AppLanguage.SYSTEM
        val language = locales[0].language
        return AppLanguage.entries.firstOrNull { it.tag == language } ?: AppLanguage.SYSTEM
    }

    fun set(context: Context, language: AppLanguage) {
        if (!supported) return
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            language.tag?.let { LocaleList.forLanguageTags(it) } ?: LocaleList.getEmptyLocaleList()
    }
}
