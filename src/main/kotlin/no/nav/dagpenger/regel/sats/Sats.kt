package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import java.math.BigDecimal

val barneTillegg = BigDecimal(17)
val dagerPerUke = BigDecimal(5)
val ukerPerÅr = BigDecimal(52)

class Sats : Beregning {
    private val nameMeBetter = OrdinærBeregning()

    override fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult {
        return nameMeBetter.beregn(grunnlag, antallBarn)
    }
}

interface Beregning {
    fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult
}

data class SatsResult(
    val dagSats: Int,
    val ukeSats: Int,
    val brukt90ProsentRegel: Boolean
)

