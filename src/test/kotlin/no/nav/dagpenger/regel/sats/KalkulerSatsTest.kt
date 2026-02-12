package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.regel.sats.helpers.februar
import no.nav.dagpenger.regel.sats.helpers.januar
import no.nav.dagpenger.regel.sats.helpers.mars
import no.nav.dagpenger.regel.sats.versjoner.KoronaBeregning
import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals

val sats = Sats()

fun calculateSats(
    grunnlag: BigDecimal,
    antallBarn: Int,
): SatsResult =
    sats.forDato(1.januar, 1.januar).beregn(
        grunnlag = Grunnlag(grunnlag, getGrunnbeløpForRegel(Regel.Grunnlag).forDato(LocalDate.now()).verdi),
        antallBarn = antallBarn,
    )

class KalkulerSatsTest {
    @Test
    fun `brukt90prosentregel er true når dagpenger med barnetillegg utgjør mer enn 90 prosent av grunnlag`() {
        val (_, _, brukt90ProsentRegel) =
            calculateSats(
                BigDecimal(70000),
                7,
            )
        assertTrue(brukt90ProsentRegel)
    }

    @Test
    fun `brukt90prosentregel er false når dagpenger med barnetillegg utgjør mindre enn 90 prosent av grunnlag`() {
        val (_, _, brukt90ProsentRegel) =
            calculateSats(
                BigDecimal(120000),
                7,
            )
        assertFalse(brukt90ProsentRegel)
    }

    @Test
    fun `Skal regne ut korrekte satser uten barnetillegg`() {
        val (dagSats, ukeSats, used90ProsentRegel) =
            calculateSats(
                BigDecimal(100000),
                0,
            )
        assertEquals(240, dagSats)
        assertEquals(1200, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `Skal bruke ny sats for barnetillegg fra og med første februar 2023`() {
        OrdinærBeregning(Barnetillegg.forDato(1.februar(2023)))
            .beregn(
                grunnlag =
                    Grunnlag(
                        BigDecimal(100000),
                        getGrunnbeløpForRegel(Regel.Grunnlag).forDato(1.februar(2023)).verdi,
                    ),
                4,
            ).also { (dagSats, ukeSats) ->
                assertEquals(240, dagSats)
                assertEquals(1731, ukeSats)
            }

        OrdinærBeregning(Barnetillegg.forDato(1.januar(2023)))
            .beregn(
                grunnlag =
                    Grunnlag(
                        BigDecimal(100000),
                        getGrunnbeløpForRegel(Regel.Grunnlag).forDato(1.januar(2023)).verdi,
                    ),
                4,
            ).also { (dagSats, ukeSats) ->
                assertEquals(240, dagSats)
                assertEquals(1540, ukeSats)
            }
    }

    @Test
    fun `Skal regne ut korrekte satser i Koronatider`() {
        val dayInTheKorona = 21.mars
        val grunnlag = BigDecimal(180374)

        val (dagSats, ukeSats, used90ProsentRegel) =
            KoronaBeregning(BigDecimal(17)).beregn(
                grunnlag = Grunnlag(grunnlag, getGrunnbeløpForRegel(Regel.Grunnlag).forDato(dayInTheKorona).verdi),
                antallBarn = 0,
            )

        assertEquals(559, dagSats)
        assertEquals(2795, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `Skal runde av dagsats før ukesats`() {
        val (dagSats, ukeSats, used90ProsentRegel) =
            calculateSats(
                BigDecimal(425625),
                0,
            )
        assertEquals(1022, dagSats)
        assertEquals(5110, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun ` Skal inkludere korrekt barnetillegg til ukesats`() {
        val (dagSats, ukeSats, used90ProsentRegel) =
            calculateSats(
                BigDecimal(100000),
                3,
            )
        assertEquals(240, dagSats)
        assertEquals(1455, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `Skal nedjustere uksesats for 90-prosent regel`() {
        val (dagSats, ukeSats, brukt90ProsentRegel) =
            calculateSats(
                BigDecimal(100000),
                10,
            )
        assertEquals(240, dagSats)
        assertEquals(1731, ukeSats)
        assertTrue(brukt90ProsentRegel)
    }

    @Test
    fun `Test grenseverdier for 90-prosentregel`() {
        val (dagSats1, ukeSats1, brukt90ProsentRegel1) =
            calculateSats(
                BigDecimal(100000),
                6,
            )
        assertEquals(240, dagSats1)
        assertEquals(1710, ukeSats1)
        assertFalse(brukt90ProsentRegel1)

        val (dagSats2, ukeSats2, brukt90ProsentRegel2) =
            calculateSats(
                BigDecimal(100000),
                7,
            )
        assertEquals(240, dagSats2)
        assertEquals(1731, ukeSats2)
        assertTrue(brukt90ProsentRegel2)
    }
}
