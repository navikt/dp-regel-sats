package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class Grunnlag(val grunnlag: BigDecimal, private val grunnbeløp: BigDecimal) {

    fun getGrunnlagMellom(nedreGrense: Double, øvreGrense: Double): BigDecimal {
        val nedreTerskel = grunnbeløp.times(nedreGrense.toBigDecimal())
        val øvreTerskel = grunnbeløp.times(øvreGrense.toBigDecimal())

        return grunnlag.min(øvreTerskel).minus(nedreTerskel).max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP)
    }
}

internal class GjeldendeGrunnbeløp(private val features: Features) {
    internal fun grunnbeløp(beregningsdato: LocalDate): BigDecimal = when {
        isThisGjusteringTest(beregningsdato) -> Grunnbeløp.GjusteringsTest
        else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(beregningsdato)
    }.verdi

    private fun isThisGjusteringTest(
        beregningsdato: LocalDate
    ): Boolean {
        val isBeregningsDatoAfterGjustering = beregningsdato.isAfter(LocalDate.of(2020, 9, 1).minusDays(1))
        return features.isEnabled("gjustering") && isBeregningsDatoAfterGjustering
    }
}
