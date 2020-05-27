package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GjeldendeGrunnbeløpTest {

    @Test
    fun ` under g-justeringstest`() {

        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp(Features(mapOf("gjustering" to true)))
        Assertions.assertEquals(gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()), BigDecimal("102000"))
    }

    @Test
    fun ` ikke g-justeringstest`() {

        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp(Features(mapOf("gjustering" to false)))
        Assertions.assertEquals(gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()), BigDecimal("99858"))
    }
}
