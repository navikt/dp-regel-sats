package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.math.RoundingMode

class Grunnlag(val grunnlag: BigDecimal, private val grunnbeløp: BigDecimal) {
    fun getGrunnlagMellom(nedreGrense: Double, øvreGrense: Double): BigDecimal {
        val nedreTerskel = grunnbeløp.times(nedreGrense.toBigDecimal())
        val øvreTerskel = grunnbeløp.times(øvreGrense.toBigDecimal())

        return grunnlag.min(øvreTerskel).minus(nedreTerskel).max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP)
    }
}
