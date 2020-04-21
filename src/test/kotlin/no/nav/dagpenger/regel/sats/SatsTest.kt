package no.nav.dagpenger.regel.sats

import io.prometheus.client.CollectorRegistry
import java.time.LocalDate
import java.time.Month
import no.nav.dagpenger.regel.sats.versjoner.KoronaBeregning
import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SatsTest {
    private val sats = Sats()

    @Test
    fun `beregningsdato for 1 Januar 2020 gir ordinære regler`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.JANUARY, 1)) is OrdinærBeregning)
    }

    @Test
    fun `beregningsdato for 20 Mars 2020 gir korona regler når feature er på`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.MARCH, 20), true) is KoronaBeregning)
    }

    @Test
    fun `beregningsdato for 20 Juli 2020 gir korona regler når feature er på`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.JULY, 20), true) is KoronaBeregning)
    }

    @Test
    fun `beregningsdato for 1 Januar 2021 gir ordinære regler`() {
        // Forskriften utløper automatisk 31. desember 2020
        assertTrue(sats.forDato(LocalDate.of(2021, Month.JANUARY, 1)) is OrdinærBeregning)
    }

    @Test
    fun `vi teller hvilken regel som blir brukt`() {
        sats.forDato(LocalDate.of(2021, Month.JANUARY, 1))
        assert(CollectorRegistry.defaultRegistry.getSampleValue(satsBeregningBruktName, arrayOf("navn"), arrayOf("OrdinærBeregning")) > 0.0)
    }
}
