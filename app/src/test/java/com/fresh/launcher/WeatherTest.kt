package com.fresh.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherTest {
    private fun weather(celsius: Double, code: Int = 0) = Weather(celsius, code, fetchedAt = 0L)

    @Test
    fun `muestra Celsius redondeado sin unidad y Fahrenheit con unidad`() {
        assertEquals("20°", weather(20.4).temperatureText(fahrenheit = false))
        assertEquals("68°F", weather(20.0).temperatureText(fahrenheit = true))
        assertEquals("-3°", weather(-2.6).temperatureText(fahrenheit = false))
    }

    @Test
    fun `agrupa los códigos WMO de Open-Meteo`() {
        assertEquals(R.string.weather_clear, weather(0.0, 0).condition)
        assertEquals(R.string.weather_partly_cloudy, weather(0.0, 2).condition)
        assertEquals(R.string.weather_fog, weather(0.0, 45).condition)
        assertEquals(R.string.weather_drizzle, weather(0.0, 53).condition)
        assertEquals(R.string.weather_rain, weather(0.0, 81).condition)
        assertEquals(R.string.weather_snow, weather(0.0, 86).condition)
        assertEquals(R.string.weather_storm, weather(0.0, 99).condition)
        // Código desconocido: se muestra como nublado en vez de fallar
        assertEquals(R.string.weather_cloudy, weather(0.0, 1234).condition)
    }
}
