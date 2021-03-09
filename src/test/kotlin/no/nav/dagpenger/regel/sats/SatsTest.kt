package no.nav.dagpenger.regel.sats

import io.prometheus.client.CollectorRegistry
import no.nav.dagpenger.regel.sats.versjoner.KoronaBeregning
import no.nav.dagpenger.regel.sats.versjoner.KoronaLærlingBeregning
import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

internal class SatsTest {
    private val sats = Sats()

    @Test
    fun `beregningsdato for 1 Januar 2020 gir ordinære regler`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.JANUARY, 1)) is OrdinærBeregning)
    }

    @Test
    fun `beregningsdato for 20 Mars 2020 gir korona ordinære regler når feature er på`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.MARCH, 20), false) is KoronaBeregning)
    }

    @Test
    fun `beregningsdato for 20 Juli 2020 gir korona ordinære regler når feature er på`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.JULY, 20), false) is KoronaBeregning)
    }

    @Test
    fun `beregningsdato for 20 Mars 2020 gir korona lærling regler når feature er på`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.MARCH, 20), true) is KoronaLærlingBeregning)
    }

    @Test
    fun `beregningsdato for 20 Juli 2020 gir korona lærling regler når feature er på`() {
        assertTrue(sats.forDato(LocalDate.of(2020, Month.JULY, 20), true) is KoronaLærlingBeregning)
    }

    @Test
    fun `beregningsdato for 1 Oktober 2021 gir ordinære regler`() {
        // utløper automatisk 30. september 2021
        assertTrue(sats.forDato(LocalDate.of(2021, Month.OCTOBER, 1)) is OrdinærBeregning)
    }

    @Test
    fun `vi teller hvilken regel som blir brukt`() {
        sats.forDato(LocalDate.of(2022, Month.JANUARY, 1))
        assert(CollectorRegistry.defaultRegistry.getSampleValue(satsBeregningBruktName, arrayOf("navn"), arrayOf("OrdinærBeregning")) > 0.0)
    }

    @Test
    fun `beregningsdato før 20 mars 2020 og regelverksdato etter 1 februar 2021 gir koronaberegning`() {
        val beregning = sats.forDato(LocalDate.of(2020, Month.MARCH, 19), regelverksdato = LocalDate.of(2021, Month.FEBRUARY, 2))
        assertTrue(beregning is KoronaBeregning)
    }
}
