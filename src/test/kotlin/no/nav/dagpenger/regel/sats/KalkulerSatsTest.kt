package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class KalkulerSatsTest {

    @Test
    fun ` Skal kunne returnere en dagsats `() {
        val resultat = calculateDagSats(100000.toBigDecimal())
        assertEquals(240.toBigDecimal().stripTrailingZeros(), resultat.stripTrailingZeros())
    }

    @Test
    fun ` Skal kunne returnere rett ukesats med barnetillegg `() {
        val resultat = calculateUkeSats(240.toBigDecimal(), 3)
        assertEquals(1455.toBigDecimal(), resultat)
    }

    @Test
    fun ` Skal kunne returnere rett ukesats uten barnetillegg `() {
        val resultat = calculateUkeSats(240.toBigDecimal(), 0)
        assertEquals(1200.toBigDecimal(), resultat)
    }
}