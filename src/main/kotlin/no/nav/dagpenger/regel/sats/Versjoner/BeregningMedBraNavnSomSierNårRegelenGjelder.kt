package no.nav.dagpenger.regel.sats.Versjoner

import no.nav.dagpenger.regel.sats.Beregning
import no.nav.dagpenger.regel.sats.SatsResult
import java.math.BigDecimal
import java.math.RoundingMode

val barneTillegg = BigDecimal(17)
val dagSatsFaktor = BigDecimal(0.0024)
val dagerPerUke = BigDecimal(5)
val ukerPerÅr = BigDecimal(52)

class BeregningMedBraNavnSomSierNårRegelenGjelder : Beregning {
    override fun beregn(grunnlag: BigDecimal, antallBarn: Int): SatsResult {
        val dagSats = (grunnlag * dagSatsFaktor).setScale(
            0,
            RoundingMode.HALF_UP
        )
        val barnetillegg = BigDecimal(antallBarn) * barneTillegg
        val ukeSats = (dagSats + barnetillegg) * dagerPerUke

        val årligDagpenger = ukeSats * ukerPerÅr
        val nittProsentAvGrunnlag = grunnlag * BigDecimal(0.9)

        if (årligDagpenger > nittProsentAvGrunnlag) {
            val redusertUkeSats = nittProsentAvGrunnlag / ukerPerÅr
            return SatsResult(
                dagSats.toInt(),
                redusertUkeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
                true
            )
        }

        return SatsResult(
            dagSats.toInt(),
            ukeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
            false
        )
    }
}

