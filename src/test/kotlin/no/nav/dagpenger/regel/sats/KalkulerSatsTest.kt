package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KalkulerSatsTest {

    @Test
    fun ` Skal kunne returnere en dagsats `() {
        val resultat = calculateDagSats(100000)
        assertEquals(240, resultat)
    }

    @Test
    fun ` Skal kunne returnere rett ukesats med barnetillegg `() {
        val resultat = calculateUkeSats(240, 3)
        assertEquals(1455, resultat)
    }

    @Test
    fun ` Skal kunne returnere rett ukesats uten barnetillegg `() {
        val resultat = calculateUkeSats(240, 0)
        assertEquals(1200, resultat)
    }
}