package no.nav.dagpenger.regel.sats.versjoner

import java.math.BigDecimal
import java.math.RoundingMode
import no.nav.dagpenger.regel.sats.Beregningsregel
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.SatsResult
import no.nav.dagpenger.regel.sats.dagerPerUke
import no.nav.dagpenger.regel.sats.ukerPerÅr

open class BarnetilleggBeregning {
    protected fun inkluderBarnetillegg(
        antallBarn: Int,
        dagSats: BigDecimal,
        grunnlag: Grunnlag,
        regelBrukt: Beregningsregel
    ): SatsResult {
        val barnetillegg = BigDecimal(antallBarn) * barneTillegg
        val ukeSats = (dagSats + barnetillegg) * dagerPerUke

        val årligDagpenger = ukeSats * ukerPerÅr
        val nittProsentAvGrunnlag = grunnlag.grunnlag * BigDecimal(0.9)

        if (årligDagpenger > nittProsentAvGrunnlag) {
            val redusertUkeSats = nittProsentAvGrunnlag / ukerPerÅr
            return SatsResult(
                dagSats = dagSats.toInt(),
                ukeSats = redusertUkeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
                brukt90ProsentRegel = true,
                beregningsregel = regelBrukt
            )
        }

        return SatsResult(
            dagSats = dagSats.toInt(),
            ukeSats = ukeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
            brukt90ProsentRegel = false,
            beregningsregel = regelBrukt
        )
    }
}

val barneTillegg = BigDecimal(17)
