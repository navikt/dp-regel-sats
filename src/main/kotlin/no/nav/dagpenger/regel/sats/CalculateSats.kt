package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.math.RoundingMode

val barneTillegg = BigDecimal(17)
val dagSatsFaktor = BigDecimal(0.0024)
val dagerPerUke = BigDecimal(5)
val ukerPerÅr = BigDecimal(52)
val dagerPerÅr = dagerPerUke * ukerPerÅr

private fun calculateSatsUnrounded(grunnlag: BigDecimal, antallBarn: Int): Triple<BigDecimal, BigDecimal, Boolean> {
    val dagSats = grunnlag * dagSatsFaktor
    val ukeSats = (dagSats + BigDecimal(antallBarn) * barneTillegg) * dagerPerUke
    val årligDagpenger = ukeSats * ukerPerÅr
    val nittProsentAvGrunnlag = grunnlag * BigDecimal(0.9)

    if (årligDagpenger > nittProsentAvGrunnlag) {
        val redusertDagSats = nittProsentAvGrunnlag / dagerPerÅr
        val redusertUkeSats = redusertDagSats * dagerPerUke
        return Triple(redusertDagSats, redusertUkeSats, true)
    }

    return Triple(dagSats, ukeSats, false)
}

fun calculateSats(grunnlag: BigDecimal, antallBarn: Int): Triple<BigDecimal, BigDecimal, Boolean> {
    return calculateSatsUnrounded(grunnlag, antallBarn).let {
        Triple(
            it.first.setScale(6, RoundingMode.HALF_UP),
            it.second.setScale(6, RoundingMode.HALF_UP),
            it.third
        )
    }
}
