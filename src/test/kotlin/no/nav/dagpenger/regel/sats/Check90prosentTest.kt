package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Check90prosentTest {

    @Test
    fun `should return true if dagpenger inklusiv barnetillegg is more than 90 procent of dagpengegrunnlag ` () {
        val resultat = ukeSatsMoreThan90PercentOfGrunnlag(7000.toBigDecimal(), 700.toBigDecimal())
        assertTrue(resultat)
    }

    @Test
    fun `should return false if dagpenger inklusiv barnetillegg less than 90 procent of dagpengegrunnlag ` () {
        val resultat = ukeSatsMoreThan90PercentOfGrunnlag(7000.toBigDecimal(), 100.toBigDecimal())
        assertFalse(resultat)
    }
}