package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GrunnlagTest {

    @Test
    fun `splitter over og under 3G`() {
        val grunnlag = Grunnlag(500000.toBigDecimal(), 100000.toBigDecimal())
        var andelUnder075G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.0,
            øvreGrense = 0.75
        )
        var andelUnder3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.0,
            øvreGrense = 3.0
        )
        var andelMellom075Gog3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.75,
            øvreGrense = 3.0
        )
        var andelOver3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 3.0,
            øvreGrense = 6.0
        )

        assertEquals(75000.toBigDecimal(), andelUnder075G)
        assertEquals(300000.toBigDecimal(), andelUnder3G)
        assertEquals(andelUnder3G.minus(andelUnder075G), andelMellom075Gog3G)
        assertEquals(200000.toBigDecimal(), andelOver3G)
    }

    @Test
    fun `splitter over og under 3G når inntekt under 3G`() {
        val grunnlag = Grunnlag(200000.toBigDecimal(), 100000.toBigDecimal())

        var andelUnder075G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.0,
            øvreGrense = 0.75
        )
        var andelUnder3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.0,
            øvreGrense = 3.0
        )
        var andelMellom075Gog3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.75,
            øvreGrense = 3.0
        )
        var andelOver3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 3.0,
            øvreGrense = 6.0
        )

        assertEquals(75000.toBigDecimal(), andelUnder075G)
        assertEquals(200000.toBigDecimal(), andelUnder3G)
        assertEquals(andelUnder3G.minus(andelUnder075G), andelMellom075Gog3G)
        assertEquals(BigDecimal.ZERO, andelOver3G)
    }

    @Test
    fun `splitter over og under 3G når inntekt under 075G`() {
        val grunnlag = Grunnlag(20000.toBigDecimal(), 100000.toBigDecimal())

        var andelUnder075G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.0,
            øvreGrense = 0.75
        )
        var andelMellom075Gog3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 0.75,
            øvreGrense = 3.0
        )
        var andelOver3G = grunnlag.getGrunnlagMellom(
            nedreGrense = 3.0,
            øvreGrense = 6.0
        )

        assertEquals(20000.toBigDecimal(), andelUnder075G)
        assertEquals(BigDecimal.ZERO, andelMellom075Gog3G)
        assertEquals(BigDecimal.ZERO, andelOver3G)
    }
}
