package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Grunnlag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KoronaBeregningTest {
    private val satsKoronaberegning = KoronaBeregning()

    @Test
    fun `Forhøyet sats periode`() {
        val regelverksDato1 = LocalDate.parse("2021-10-31")
        val beregningsDato1 = LocalDate.parse("2020-11-25")
        assertTrue {
            satsKoronaberegning.isActive(beregningsDato1, regelverksDato1)
        }
//        val regelverksDato2 = LocalDate.parse("2020-03-03")
//        val beregningsDato2 = LocalDate.parse("2021-10-01")
//        assertTrue {
//            satsKoronaberegning.isActive(beregningsDato2, regelverksDato2)
//        }
    }

    @Test
    fun `gir økt dagsats for de under 3G`() {
        val (dagSats, ukeSats, used90ProsentRegel) = satsKoronaberegning.beregn(
            grunnlag = Grunnlag(BigDecimal(100000), BigDecimal(100000)),
            antallBarn = 0
        )
        assertEquals(308, dagSats)
        assertEquals(1540, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `gir økt dagsats for de over 3G`() {
        val (dagSats, ukeSats, used90ProsentRegel) = satsKoronaberegning.beregn(
            grunnlag = Grunnlag(BigDecimal(500000), BigDecimal(100000)),
            antallBarn = 0
        )
        assertEquals(1403, dagSats)
        assertEquals(7015, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `gir økt dagsats for de over 6G, men ikke mer`() {
        val (dagSats, ukeSats, used90ProsentRegel) = satsKoronaberegning.beregn(
            grunnlag = Grunnlag(BigDecimal(5000000), BigDecimal(100000)),
            antallBarn = 0
        )
        assertEquals(1643, dagSats)
        assertEquals(8215, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun ` Skal ikke  avrunde del satsene men avrunde når delsatsene summeres `() {
        val (dagSats, ukeSats, used90ProsentRegel) = satsKoronaberegning.beregn(
            grunnlag = Grunnlag(BigDecimal(305650), BigDecimal(99858)),
            antallBarn = 0
        )
        assertEquals(936, dagSats)
        assertEquals(4680, ukeSats)
        assertFalse(used90ProsentRegel)
    }
}
