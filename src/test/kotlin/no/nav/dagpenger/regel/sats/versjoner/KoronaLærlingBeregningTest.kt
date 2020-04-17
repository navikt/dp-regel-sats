package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.regel.sats.Grunnlag
import org.junit.jupiter.api.Test
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
        val (dagSats, ukeSats, used90ProsentRegel) = sats.beregn(
            grunnlag = Grunnlag(grunnlag = 200_000.toBigDecimal(), grunnbeløp = Grunnbeløp.FastsattI2019.verdi),
            antallBarn = 0
        )
        assertEquals(697, dagSats)
        assertEquals(697 * 5, ukeSats)
        assertFalse(used90ProsentRegel)
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
        val (dagSats, _, used90ProsentRegel) = sats.beregn(
            grunnlag = Grunnlag(grunnlag = 100_000.toBigDecimal(), grunnbeløp = Grunnbeløp.FastsattI2019.verdi),
            antallBarn = 5
        )
        assertEquals(385, dagSats)
        assertFalse(used90ProsentRegel)
    }
}