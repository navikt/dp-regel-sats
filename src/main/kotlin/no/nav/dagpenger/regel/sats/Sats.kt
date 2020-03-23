package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.regel.sats.Versjoner.BeregningMedBraNavnSomSierNårRegelenGjelder
import java.math.BigDecimal

val nameMeBetter = BeregningMedBraNavnSomSierNårRegelenGjelder()

class Sats : Beregning {
    override fun beregn(grunnlag: BigDecimal, antallBarn: Int): SatsResult {
        return nameMeBetter.beregn(grunnlag, antallBarn)
    }
}

interface Beregning {
    fun beregn(grunnlag: BigDecimal, antallBarn: Int): SatsResult
}

data class SatsResult(
    val dagSats: Int,
    val ukeSats: Int,
    val brukt90ProsentRegel: Boolean
)