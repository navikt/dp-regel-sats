package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.regel.sats.Grunnlag
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class KoronaLærlingBeregningTest {
    private val sats = KoronaLærlingBeregning()

    @Test
    fun `gir 100 % av grunnlaget under 1,5G, og bruker dermed ikke 90 % regelen`() {
        val (dagSats, ukeSats, used90ProsentRegel) = sats.beregn(
            grunnlag = Grunnlag(grunnlag = 100_000.toBigDecimal(), grunnbeløp = Grunnbeløp.FastsattI2019.verdi),
            antallBarn = 0
        )
        assertEquals(385, dagSats)
        assertEquals(385 * 5, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `gir 100 % av grunnlaget under 1,5G og 62,4 % av grunnlaget opp til 6G `() {
        val (dagSats, _, _) = sats.beregn(
            grunnlag = Grunnlag(grunnlag = 599097.toBigDecimal(), grunnbeløp = Grunnbeløp.FastsattI2020.verdi),
            antallBarn = 3
        )
        assertEquals(1658, dagSats)
    }

    @Test
    fun `beregener ikke sats for grunnlag over 6G`() {
        val (maksDagsats, _, _) = sats.beregn(
            grunnlag = Grunnlag(
                grunnlag = Grunnbeløp.FastsattI2019.verdi.times(6.toBigDecimal()),
                grunnbeløp = Grunnbeløp.FastsattI2019.verdi
            ),
            antallBarn = 0
        )

        val (dagsatsMedGrunnlagOver6G, _, _) = sats.beregn(
            grunnlag = Grunnlag(
                grunnlag = Grunnbeløp.FastsattI2019.verdi.times(10.toBigDecimal()),
                grunnbeløp = Grunnbeløp.FastsattI2019.verdi
            ),
            antallBarn = 0
        )
        assertEquals(maksDagsats, dagsatsMedGrunnlagOver6G)
    }

    @Test
    fun `lærlinger kvalifiserer ikke til barnetillegg`() {
        val (dagSats, ukeSats, used90ProsentRegel) = sats.beregn(
            grunnlag = Grunnlag(grunnlag = 100_000.toBigDecimal(), grunnbeløp = Grunnbeløp.FastsattI2019.verdi),
            antallBarn = 5
        )
        assertEquals(385, dagSats)
        assertEquals(1925, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun ` Skal ikke  avrunde del satsene men avrunde når delsatsene summeres `() {
        val (dagSats, ukeSats) = sats.beregn(
            grunnlag = Grunnlag(BigDecimal(239987), BigDecimal(99858)),
            antallBarn = 0
        )
        assertEquals(793, dagSats)
        assertEquals(3965, ukeSats)
    }

    @Test
    fun `er aktiv til fra 20 mars 2020 til 31 mars 2022`() {
        val beregningsdato = LocalDate.of(2021, 1, 1)
        assertTrue(sats.isActive(beregningsdato, LocalDate.of(2020, 3, 20)))
        assertTrue(sats.isActive(beregningsdato, LocalDate.of(2022, 3, 31)))
        assertFalse(sats.isActive(beregningsdato, LocalDate.of(2020, 2, 29)))
        assertFalse(sats.isActive(beregningsdato, LocalDate.of(2022, 4, 1)))
    }
}
