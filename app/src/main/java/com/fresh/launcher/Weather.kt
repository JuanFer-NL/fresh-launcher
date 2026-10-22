package com.fresh.launcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.StringRes
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/** Se guarda siempre en Celsius; la unidad elegida en la configuración se aplica al mostrarlo. */
data class Weather(
    val celsius: Double,
    val code: Int,
    val fetchedAt: Long,
) {
    fun temperatureText(fahrenheit: Boolean): String =
        if (fahrenheit) "${(celsius * 9 / 5 + 32).roundToInt()}°F" else "${celsius.roundToInt()}°"

    /** Descripción según el código WMO que devuelve Open-Meteo. */
    @get:StringRes
    val condition: Int
        get() = when (code) {
            0 -> R.string.weather_clear
            1, 2 -> R.string.weather_partly_cloudy
            3 -> R.string.weather_cloudy
            45, 48 -> R.string.weather_fog
            in 51..57 -> R.string.weather_drizzle
            in 61..67, in 80..82 -> R.string.weather_rain
            in 71..77, 85, 86 -> R.string.weather_snow
            in 95..99 -> R.string.weather_storm
            else -> R.string.weather_cloudy
        }
}

fun hasLocationPermission(context: Context): Boolean =
    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

class WeatherRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("weather", Context.MODE_PRIVATE)

    fun cached(): Weather? {
        if (!prefs.contains("celsius")) return null
        return Weather(
            celsius = prefs.getFloat("celsius", 0f).toDouble(),
            code = prefs.getInt("code", 3),
            fetchedAt = prefs.getLong("at", 0),
        )
    }

    /** Devuelve el clima nuevo, o null si no hay permiso, ubicación o red. */
    suspend fun refresh(): Weather? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null
        val location = currentLocation() ?: return@withContext null
        runCatching { fetch(location) }.getOrNull()?.also { save(it) }
    }

    fun isStale(weather: Weather?): Boolean =
        weather == null || System.currentTimeMillis() - weather.fetchedAt > REFRESH_MS

    private fun fetch(location: Location): Weather {
        // Dos decimales (~1 km): no hace falta mandar la ubicación exacta
        val lat = "%.2f".format(Locale.US, location.latitude)
        val lon = "%.2f".format(Locale.US, location.longitude)
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,weather_code&temperature_unit=celsius",
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
        }
        try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            return Weather(
                celsius = current.getDouble("temperature_2m"),
                code = current.getInt("weather_code"),
                fetchedAt = System.currentTimeMillis(),
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun save(weather: Weather) {
        prefs.edit {
            putFloat("celsius", weather.celsius.toFloat())
            putInt("code", weather.code)
            putLong("at", weather.fetchedAt)
        }
    }

    @Suppress("MissingPermission") // verificado en refresh()
    private suspend fun currentLocation(): Location? {
        val manager = context.getSystemService(LocationManager::class.java)
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }.filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        val recent = providers
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (recent != null && System.currentTimeMillis() - recent.time < LOCATION_MAX_AGE_MS) return recent

        val provider = providers.firstOrNull { it != LocationManager.PASSIVE_PROVIDER } ?: return recent
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return recent
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { cont ->
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                manager.getCurrentLocation(provider, signal, context.mainExecutor) { cont.resume(it) }
            }
        } ?: recent
    }

    companion object {
        private const val REFRESH_MS = 30 * 60 * 1000L
        private const val LOCATION_MAX_AGE_MS = 2 * 60 * 60 * 1000L
    }
}
