package no.nav.dagpenger.regel.sats

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class KalkulerSatsTest {

    @Test
    fun `brukt90prosentregel er true når dagpenger med barnetillegg utgjør mer enn 90 prosent av grunnlag`() {
        val (_, _, brukt90ProsentRegel) = calculateSats(BigDecimal(70000), 7)
        assertTrue(brukt90ProsentRegel)
    }

    @Test
    fun `brukt90prosentregel er false når dagpenger med barnetillegg utgjør mindre enn 90 prosent av grunnlag`() {
        val (_, _, brukt90ProsentRegel) = calculateSats(BigDecimal(120000), 7)
        assertFalse(brukt90ProsentRegel)
    }

    @Test
    fun `Skal regne ut korrekte satser uten barnetillegg`() {
        val (dagSats, ukeSats, used90ProsentRegel) = calculateSats(BigDecimal(100000), 0)
        assertEquals(240, dagSats)
        assertEquals(1200, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun ` Skal inkludere korrekt barnetillegg til ukesats`() {
        val (dagSats, ukeSats, used90ProsentRegel) = calculateSats(BigDecimal(100000), 3)
        assertEquals(240, dagSats)
        assertEquals(1455, ukeSats)
        assertFalse(used90ProsentRegel)
    }

    @Test
    fun `Skal nedjustere dagsats og ukeSats for 90-prosent regel`() {
        val (dagSats, ukeSats, brukt90ProsentRegel) = calculateSats(BigDecimal(100000), 10)
        assertEquals(346, dagSats)
        assertEquals(1731, ukeSats)
        assertTrue(brukt90ProsentRegel)
    }

    @Test
    fun `Test grenseverdier for 90-prosentregel`() {
        val (dagSats1, ukeSats1, brukt90ProsentRegel1) = calculateSats(BigDecimal(96086), 6)
        assertEquals(333, dagSats1)
        assertEquals(1663, ukeSats1)
        assertTrue(brukt90ProsentRegel1)

        val (dagSats2, ukeSats2, brukt90ProsentRegel2) = calculateSats(BigDecimal(96087), 6)
        assertEquals(231, dagSats2)
        assertEquals(1663, ukeSats2)
        assertFalse(brukt90ProsentRegel2)
    }
}