package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class GjeldendeGrunnbeløpTest {

    @Test
    fun ` under g-justeringstest`() {
        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp(Features(mapOf("gjustering" to true)))
        assertEquals(gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()), BigDecimal("101351"))
    }

    @Test
    fun ` ikke g-justeringstest`() {
        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp(Features(mapOf("gjustering" to false)))
        assertEquals(gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()), BigDecimal("99858"))
    }
}
