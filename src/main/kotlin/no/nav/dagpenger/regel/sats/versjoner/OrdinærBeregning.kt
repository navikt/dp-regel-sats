package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Beregning
import no.nav.dagpenger.regel.sats.Beregningsregel
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.dagerPerUke
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class OrdinærBeregning : Beregning() {

    // 62.4% dekning/ 260 dager / 100
    private val dagSatsFaktor = BigDecimal(0.0024)
    override val regelBrukt: Beregningsregel = Beregningsregel.ORDINAER

    override fun isActive(beregningsdato: LocalDate, regelverksdato: LocalDate): Boolean = true

    override fun ukeSats(dagSats: BigDecimal, antallBarn: Int): BigDecimal {
        val barnetillegg = BigDecimal(antallBarn) * barneTillegg
        return (dagSats + barnetillegg) * dagerPerUke
    }

    override fun dagSats(grunnlag: Grunnlag): BigDecimal {
        return (grunnlag.grunnlag * dagSatsFaktor).setScale(
            0,
            RoundingMode.HALF_UP
        )
    }
}
