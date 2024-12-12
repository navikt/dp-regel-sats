package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class BarnetilleggTest {
    @Test
    fun `etter 2025`() {
        val dato = LocalDate.of(2025, 1, 1)
        val result = Barnetillegg.forDato(dato)
        assertEquals(BigDecimal(37), result)
    }

    @Test
    fun `etter 2024`() {
        val dato = LocalDate.of(2024, 1, 1)
        val result = Barnetillegg.forDato(dato)
        assertEquals(BigDecimal(36), result)
    }

    @Test
    fun `mellom februar og desember 2023`() {
        val dato = LocalDate.of(2023, 2, 1)
        val result = Barnetillegg.forDato(dato)
        assertEquals(BigDecimal(35), result)
    }

    @Test
    fun `før februar 2023`() {
        val dato = LocalDate.MIN
        val result = Barnetillegg.forDato(dato)
        assertEquals(BigDecimal(17), result)
    }
}
