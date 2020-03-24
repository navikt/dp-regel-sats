package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.regel.sats.versjoner.KoronaBeregning
import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import java.math.BigDecimal
import java.time.LocalDate

val dagerPerUke = BigDecimal(5)
val ukerPerÅr = BigDecimal(52)

class Sats {
    private val ordinærBeregning = OrdinærBeregning()
    private val koronaBeregning = KoronaBeregning()

    fun forDato(beregningsdato: LocalDate, koronaToggle: Boolean = false): Beregning =
        when {
            koronaToggle && koronaBeregning.isActive(beregningsdato) -> koronaBeregning
            else -> ordinærBeregning
        }
}

interface Beregning {
    fun isActive(beregningstidspunkt: LocalDate): Boolean
    fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult
}

data class SatsResult(
    val dagSats: Int,
    val ukeSats: Int,
    val brukt90ProsentRegel: Boolean
)
