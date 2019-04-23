package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.math.RoundingMode

val barneTillegg = BigDecimal(17)
val dagSatsFaktor = BigDecimal(0.0024)
val dagerPerUke = BigDecimal(5)
val ukerPerÅr = BigDecimal(52)
val dagerPerÅr = dagerPerUke * ukerPerÅr

fun calculateSats(grunnlag: BigDecimal, antallBarn: Int): SatsResult {
    val dagSats = grunnlag * dagSatsFaktor + BigDecimal(antallBarn) * barneTillegg
    val ukeSats = dagSats * dagerPerUke
    val årligDagpenger = ukeSats * ukerPerÅr
    val nittProsentAvGrunnlag = grunnlag * BigDecimal(0.9)

    if (årligDagpenger > nittProsentAvGrunnlag) {
        val redusertDagSats = nittProsentAvGrunnlag / dagerPerÅr
        val redusertUkeSats = redusertDagSats * dagerPerUke
        return SatsResult(
            redusertDagSats.setScale(0, RoundingMode.HALF_UP).toInt(),
            redusertUkeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
            true)
    }

    return SatsResult(
        dagSats.setScale(0, RoundingMode.HALF_UP).toInt(),
        ukeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
        false)
}

data class SatsResult(
    val dagSats: Int,
    val ukeSats: Int,
    val brukt90ProsentRegel: Boolean
)
