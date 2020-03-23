package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.SatsResult
import no.nav.dagpenger.regel.sats.barneTillegg
import no.nav.dagpenger.regel.sats.dagerPerUke
import no.nav.dagpenger.regel.sats.ukerPerÅr
import java.math.BigDecimal
import java.math.RoundingMode

open class BarnetilleggBeregning {
    protected fun inkluderBarnetillegg(
        antallBarn: Int,
        dagSats: BigDecimal,
        grunnlag: Grunnlag
    ): SatsResult {
        val barnetillegg = BigDecimal(antallBarn) * barneTillegg
        val ukeSats = (dagSats + barnetillegg) * dagerPerUke

        val årligDagpenger = ukeSats * ukerPerÅr
        val nittProsentAvGrunnlag = grunnlag.grunnlag * BigDecimal(0.9)

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