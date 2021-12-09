package no.nav.dagpenger.regel.sats

import io.prometheus.client.CollectorRegistry
import no.nav.dagpenger.regel.sats.versjoner.KoronaBeregning
import no.nav.dagpenger.regel.sats.versjoner.KoronaLærlingBeregning
import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertEquals

internal class SatsTest {
    private val sats = Sats()

    private val beregningsdato = LocalDate.of(2021, 1, 1)

    @Test
    fun `regelverksdato for 1 Januar 2020 gir ordinære regler`() {
        assertTrue(sats.forDato(beregningsdato, LocalDate.of(2020, Month.JANUARY, 1)) is OrdinærBeregning)
    }

    @Test
    fun `regelverksdato for 20 Mars 2020 gir korona ordinære regler når feature er på`() {
        assertTrue(sats.forDato(beregningsdato, LocalDate.of(2020, Month.MARCH, 20), false) is KoronaBeregning)
    }

    @Test
    fun `regelverksdato for 20 Juli 2020 gir korona ordinære regler når feature er på`() {
        assertTrue(sats.forDato(beregningsdato, LocalDate.of(2020, Month.JULY, 20), false) is KoronaBeregning)
    }

    @Test
    fun `regelverksdato for 20 Mars 2020 gir korona lærling regler når feature er på`() {
        assertTrue(sats.forDato(beregningsdato, LocalDate.of(2020, Month.MARCH, 20), true) is KoronaLærlingBeregning)
    }

    @Test
    fun `regelverksdato for 20 Juli 2020 gir korona lærling regler når feature er på`() {
        assertTrue(sats.forDato(beregningsdato, LocalDate.of(2020, Month.JULY, 20), true) is KoronaLærlingBeregning)
    }

    @Test
    fun `regelverksdato for 20 Juli 2020 gir korona regler `() {
        assertTrue(sats.forDato(beregningsdato, LocalDate.of(2021, Month.OCTOBER, 31)) is KoronaBeregning)
    }

    @Test
    fun `regelverksdato for 1 februar 2022 gir ordinære regler`() {
        assertTrue(sats.forDato(beregningsdato, LocalDate.of(2022, Month.FEBRUARY, 1)) is OrdinærBeregning)
    }

    @ParameterizedTest
    @CsvSource(
        "2021-10-31, 2020-11-25, KoronaBeregning",
        "2020-03-19, 2020-11-25, OrdinærBeregning",
        "2022-01-01, 2020-03-19, KoronaBeregning",
        "2022-02-01, 2020-03-19, OrdinærBeregning",
        "2021-10-31, 2020-03-19, KoronaBeregning"
    )
    fun `Forhøyet sats regelverk`(regelverksdato: String, beregningsdato: String, regel: String) {
        assertEquals(regel, sats.forDato(LocalDate.parse(beregningsdato), LocalDate.parse(regelverksdato)).javaClass.simpleName)
    }

    @Test
    fun `vi teller hvilken regel som blir brukt`() {
        sats.forDato(beregningsdato, LocalDate.of(2022, Month.JANUARY, 1))
        assert(CollectorRegistry.defaultRegistry.getSampleValue(satsBeregningBruktName, arrayOf("navn"), arrayOf("OrdinærBeregning")) > 0.0)
    }
}
