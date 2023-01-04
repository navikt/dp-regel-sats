package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Beregning
import no.nav.dagpenger.regel.sats.Beregningsregel
import no.nav.dagpenger.regel.sats.Dekningsgrad
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.antallDesimaler
import no.nav.dagpenger.regel.sats.dagerPerUke
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month

// Defineres her: https://lovdata.no/forskrift/2020-03-20-368/§2-3 andre ledd

class KoronaLærlingBeregning : Beregning() {

    companion object {
        private val fom = LocalDate.of(2020, Month.MARCH, 20)
        private val tom = LocalDate.of(2022, Month.MARCH, 31)
        private val periode = fom..tom
    }

    override fun skalReduseres(årligDagpenger: BigDecimal, nittProsentAvGrunnlag: BigDecimal) = false

    private val dagsatsfaktorUnder1Komma5G = Dekningsgrad(dekningsgrad = 100.0).getDagSatsFaktor()
    private val dagsatsfaktorOver1Komma5G = Dekningsgrad(dekningsgrad = 62.4).getDagSatsFaktor()
    override val regelBrukt: Beregningsregel = Beregningsregel.KORONA_LAERLING

    override fun isActive(beregningsdato: LocalDate, regelverksdato: LocalDate): Boolean = regelverksdato in periode
    override fun ukeSats(dagSats: BigDecimal, antallBarn: Int): BigDecimal = dagSats * dagerPerUke

    override fun dagSats(grunnlag: Grunnlag): BigDecimal {
        val andelNedre = grunnlag.getGrunnlagMellom(0.0, 1.5)
        val andelØvre = grunnlag.getGrunnlagMellom(1.5, 6.0)

        val dagSatsUnder1komma5G = getDagSats(andelNedre, dagsatsfaktorUnder1Komma5G)
        val dagSatsOver1komma5G = getDagSats(andelØvre, dagsatsfaktorOver1Komma5G)

        return (dagSatsUnder1komma5G + dagSatsOver1komma5G).setScale(0, RoundingMode.HALF_UP)
    }

    private fun getDagSats(grunnlag: BigDecimal, dagSatsFaktor: BigDecimal): BigDecimal = grunnlag.multiply(dagSatsFaktor, MathContext(antallDesimaler))
}
