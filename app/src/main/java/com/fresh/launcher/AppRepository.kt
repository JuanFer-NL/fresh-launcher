package com.fresh.launcher

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import androidx.core.content.edit
import java.text.Collator
import java.text.Normalizer

data class AppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
) {
    /** Identificador estable para guardar favoritas. */
    val key: String get() = "$packageName/$activityName"

    /** Etiqueta sin tildes ni mayúsculas, para buscar "camara" y encontrar "Cámara". */
    val searchKey: String = label.normalizedForSearch()
}

fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()

/** Apps con ícono en el launcher, ordenadas alfabéticamente según el idioma del teléfono. */
fun loadLaunchableApps(context: Context): List<AppEntry> {
    val launcherApps = context.getSystemService(LauncherApps::class.java)
    val collator = Collator.getInstance()
    return launcherApps.getActivityList(null, Process.myUserHandle())
        .filter { it.applicationInfo.packageName != context.packageName }
        .map {
            AppEntry(
                label = it.label.toString(),
                packageName = it.applicationInfo.packageName,
                activityName = it.name,
            )
        }
        .sortedWith(compareBy(collator) { it.label })
}

fun launchApp(context: Context, app: AppEntry) {
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setClassName(app.packageName, app.activityName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // La app se desinstaló mientras la lista estaba en pantalla; se refresca en el próximo ON_RESUME
    } catch (_: SecurityException) {
    }
}

fun openAppInfo(context: Context, app: AppEntry) {
    context.getSystemService(LauncherApps::class.java).startAppDetailsActivity(
        ComponentName(app.packageName, app.activityName),
        Process.myUserHandle(),
        null,
        null,
    )
}

/** Favoritas del inicio, en orden, guardadas como claves "paquete/actividad". */
class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun load(): List<String> =
        prefs.getString(KEY, null)?.split('\n')?.filter { it.isNotBlank() } ?: emptyList()

    fun save(keys: List<String>) {
        prefs.edit { putString(KEY, keys.joinToString("\n")) }
    }

    companion object {
        const val MAX = 7
        private const val KEY = "order"
    }
}
