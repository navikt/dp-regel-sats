package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

abstract class Beregning {
    abstract val regelBrukt: Beregningsregel

    abstract fun isActive(
        beregningsdato: LocalDate,
        regelverksdato: LocalDate,
    ): Boolean

    abstract fun ukeSats(
        dagSats: BigDecimal,
        antallBarn: Int,
    ): BigDecimal

    abstract fun dagSats(grunnlag: Grunnlag): BigDecimal

    fun beregn(
        grunnlag: Grunnlag,
        antallBarn: Int,
    ): SatsResult {
        val dagSats = dagSats(grunnlag)
        val ukeSats = ukeSats(dagSats(grunnlag), antallBarn)

        val årligDagpenger = ukeSats * ukerPerÅr
        val nittProsentAvGrunnlag = grunnlag.grunnlag * BigDecimal(0.9)

        if (skalReduseres(årligDagpenger, nittProsentAvGrunnlag)) {
            val redusertUkeSats = nittProsentAvGrunnlag / ukerPerÅr
            return SatsResult(
                dagSats = dagSats.toInt(),
                ukeSats = redusertUkeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
                brukt90ProsentRegel = true,
                beregningsregel = regelBrukt,
            )
        }

        return SatsResult(
            dagSats = dagSats.toInt(),
            ukeSats = ukeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
            brukt90ProsentRegel = false,
            beregningsregel = regelBrukt,
        )
    }

    protected open fun skalReduseres(
        årligDagpenger: BigDecimal,
        nittProsentAvGrunnlag: BigDecimal,
    ) = årligDagpenger > nittProsentAvGrunnlag
}
