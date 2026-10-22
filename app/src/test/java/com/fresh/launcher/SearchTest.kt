package com.fresh.launcher

import com.fresh.launcher.ui.filterApps
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTest {
    private fun app(label: String) = AppEntry(label, "pkg.${label.lowercase()}", "Main")

    private val apps = listOf(app("Cámara"), app("Calculadora"), app("Google Maps"), app("Mapas Offline"), app("Reloj"))

    @Test
    fun `normaliza tildes, diéresis, eñes y mayúsculas`() {
        assertEquals("camara", "Cámara".normalizedForSearch())
        assertEquals("pinguino", "Pingüino".normalizedForSearch())
        assertEquals("nandu", "ÑANDÚ".normalizedForSearch())
    }

    @Test
    fun `encuentra sin tildes`() {
        assertEquals(listOf("Cámara"), filterApps(apps, "camara").map { it.label })
    }

    @Test
    fun `primero las que empiezan con la búsqueda o tienen una palabra que empieza con ella`() {
        // "Google Maps" y "Mapas Offline" tienen una palabra que empieza con "ma"; "Cámara" solo la contiene
        assertEquals(
            listOf("Google Maps", "Mapas Offline", "Cámara"),
            filterApps(apps, "ma").map { it.label },
        )
    }

    @Test
    fun `búsqueda vacía devuelve todas en el mismo orden`() {
        assertEquals(apps, filterApps(apps, "  "))
    }

    @Test
    fun `sin coincidencias devuelve una lista vacía`() {
        assertEquals(emptyList<AppEntry>(), filterApps(apps, "xyz"))
    }
}
