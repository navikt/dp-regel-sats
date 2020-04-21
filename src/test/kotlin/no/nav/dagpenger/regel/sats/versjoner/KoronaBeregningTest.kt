package no.nav.dagpenger.regel.sats.versjoner

import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import no.nav.dagpenger.regel.sats.Grunnlag
import org.junit.jupiter.api.Test

internal class KoronaBeregningTest {
    private val sats = KoronaBeregning()

    @Test
    fun `gir økt dagsats for de under 3G`() {
        val (dagSats, ukeSats, used90ProsentRegel) = sats.beregn(
            grunnlag = Grunnlag(BigDecimal(100000), BigDecimal(100000)),
            antallBarn = 0
        )
        assertEquals(308, dagSats)
        assertEquals(1540, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `gir økt dagsats for de over 3G`() {
        val (dagSats, ukeSats, used90ProsentRegel) = sats.beregn(
            grunnlag = Grunnlag(BigDecimal(500000), BigDecimal(100000)),
            antallBarn = 0
        )
        assertEquals(1403, dagSats)
        assertEquals(7015, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `gir økt dagsats for de over 6G, men ikke mer`() {
        val (dagSats, ukeSats, used90ProsentRegel) = sats.beregn(
            grunnlag = Grunnlag(BigDecimal(5000000), BigDecimal(100000)),
            antallBarn = 0
        )
        assertEquals(1643, dagSats)
        assertEquals(8215, ukeSats)
        assertFalse(used90ProsentRegel)
    }
}
