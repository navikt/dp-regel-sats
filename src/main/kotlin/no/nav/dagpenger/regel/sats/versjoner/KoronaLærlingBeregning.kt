package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Beregning
import no.nav.dagpenger.regel.sats.Beregningsregel
import no.nav.dagpenger.regel.sats.Dekningsgrad
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.SatsResult
import no.nav.dagpenger.regel.sats.antallDesimaler
import no.nav.dagpenger.regel.sats.dagerPerUke
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month

// Defineres her: https://lovdata.no/forskrift/2020-03-20-368/§2-3 andre ledd

class KoronaLærlingBeregning : Beregning {

    companion object {
        private val fom = LocalDate.of(2020, Month.MARCH, 20)
        private val tom = LocalDate.of(2021, Month.DECEMBER, 31)
        private val periode = fom..tom
    }

    private val REGEL_NAVN = Beregningsregel.KORONA_LAERLING

    private val dagsatsfaktorUnder1Komma5G = Dekningsgrad(dekningsgrad = 100.0).getDagSatsFaktor()
    private val dagsatsfaktorOver1Komma5G = Dekningsgrad(dekningsgrad = 62.4).getDagSatsFaktor()
    override fun isActive(beregningstidspunkt: LocalDate): Boolean = beregningstidspunkt in periode

    override fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult {
        val andelNedre = grunnlag.getGrunnlagMellom(0.0, 1.5)
        val andelØvre = grunnlag.getGrunnlagMellom(1.5, 6.0)

        val dagSatsUnder1komma5G = getDagSats(andelNedre, dagsatsfaktorUnder1Komma5G)
        val dagSatsOver1komma5G = getDagSats(andelØvre, dagsatsfaktorOver1Komma5G)

        val dagSats = (dagSatsUnder1komma5G + dagSatsOver1komma5G).setScale(0, RoundingMode.HALF_UP)
        val ukeSats = dagSats * dagerPerUke

        return SatsResult(
            dagSats = dagSats.toInt(),
            ukeSats = ukeSats.setScale(0, RoundingMode.HALF_UP).toInt(),
            brukt90ProsentRegel = false,
            beregningsregel = REGEL_NAVN
        )
    }

    private fun getDagSats(grunnlag: BigDecimal, dagSatsFaktor: BigDecimal): BigDecimal = grunnlag.multiply(dagSatsFaktor, MathContext(antallDesimaler))
}
