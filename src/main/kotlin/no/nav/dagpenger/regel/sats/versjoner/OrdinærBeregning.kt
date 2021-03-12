package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Beregning
import no.nav.dagpenger.regel.sats.Beregningsregel
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.SatsResult
import no.nav.dagpenger.regel.sats.dagerPerUke
import no.nav.dagpenger.regel.sats.ukerPerÅr
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class OrdinærBeregning : Beregning {
    private val REGEL_NAVN = Beregningsregel.ORDINAER

    // 62.4% dekning/ 260 dager / 100
    private val dagSatsFaktor = BigDecimal(0.0024)

    override fun isActive(beregningsdato: LocalDate, regelverksdato: LocalDate?): Boolean = true

    override fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult {
        val dagSats = (grunnlag.grunnlag * dagSatsFaktor).setScale(
            0,
            RoundingMode.HALF_UP
        )
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
                beregningsregel = REGEL_NAVN
            )
        }

        return SatsResult(
            dagSats = dagSats.toInt(),
            ukeSats = ukeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
            brukt90ProsentRegel = false,
            beregningsregel = REGEL_NAVN
        )
    }
}
