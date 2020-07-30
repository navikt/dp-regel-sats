package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode

internal class DekningsgradTest {

    @Test
    fun `regner ut dagsats faktor riktig for ordinære regler`() {
        val dekningsgrad = Dekningsgrad(
            dekningsgrad = 62.4,
            virkedager = 260
        )

        assertEquals(
            BigDecimal(0.0024).setScale(4, RoundingMode.HALF_UP),
            dekningsgrad.getDagSatsFaktor().setScale(4, RoundingMode.HALF_UP)
        )
    }

    @Test
    fun `regner ut dagsats faktor riktig for korona regler`() {
        val dekningsgrad = Dekningsgrad(
            dekningsgrad = 80.0,
            virkedager = 260
        )

        assertEquals(
            BigDecimal(0.0031).setScale(4, RoundingMode.HALF_UP),
            dekningsgrad.getDagSatsFaktor().setScale(4, RoundingMode.HALF_UP)
        )
    }
}
