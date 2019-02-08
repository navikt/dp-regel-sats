package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SatsInputTest {

    @Test
    fun `Process behov with grunnlag and antallBarn`() {
        val behov = SubsumsjonsBehov.Builder()
            .dagpengeGrunnlag(500)
            .antallBarn(1)
            .build()

        assertTrue(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not process behov without grunnlag`() {
        val behov = SubsumsjonsBehov.Builder()
            .antallBarn(1)
            .build()

        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not process behov without antallBarn`() {
        val behov = SubsumsjonsBehov.Builder()
            .antallBarn(1)
            .build()

        assertFalse(shouldBeProcessed(behov))
    }

    @Test
    fun `Do not (re)process behov with satsSubsumsjon`() {
        val behov = SubsumsjonsBehov.Builder()
            .dagpengeGrunnlag(500)
            .antallBarn(1)
            .satsResultat(SatsResultat("aa", "bb", "Sats.v1", 200))
            .build()

        assertFalse(shouldBeProcessed(behov))
    }
}
