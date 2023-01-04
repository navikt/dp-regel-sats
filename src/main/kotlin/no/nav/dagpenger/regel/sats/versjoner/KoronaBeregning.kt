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

private val fom = LocalDate.of(2020, Month.MARCH, 20)
private val tom = LocalDate.of(2022, Month.MARCH, 31)
private val periode = fom..tom

private val forhøyetSatsFom = LocalDate.of(2021, Month.FEBRUARY, 1)
private val forhøyetSatsPeriode = forhøyetSatsFom..tom

private fun LocalDate.erForhøyetSatsPeriode() = this in forhøyetSatsPeriode
private fun LocalDate.erKoronaPeriode() = this in periode

// Innført av https://lovdata.no/dokument/LTI/forskrift/2020-03-20-368\
internal class KoronaBeregning(private val barneTillegg: BigDecimal) : Beregning() {

    private val dagSatsFaktorUnder3G = Dekningsgrad(dekningsgrad = 80.0).getDagSatsFaktor()
    private val dagSatsFaktorOver3G = Dekningsgrad(dekningsgrad = 62.4).getDagSatsFaktor()
    override val regelBrukt: Beregningsregel = Beregningsregel.KORONA

    override fun isActive(beregningsdato: LocalDate, regelverksdato: LocalDate): Boolean =
        regelverksdato.erKoronaPeriode() && beregningsdato.isAfter(LocalDate.of(2020, Month.MARCH, 19)) ||
            (beregningsdato.isBefore(LocalDate.of(2020, Month.MARCH, 20)) && regelverksdato.erForhøyetSatsPeriode())

    override fun ukeSats(dagSats: BigDecimal, antallBarn: Int): BigDecimal {
        val barnetillegg = BigDecimal(antallBarn) * barneTillegg
        return (dagSats + barnetillegg) * dagerPerUke
    }

    override fun dagSats(grunnlag: Grunnlag): BigDecimal {
        val andelNedre = grunnlag.getGrunnlagMellom(0.0, 3.0)
        val andelØvre = grunnlag.getGrunnlagMellom(3.0, 6.0)

        val dagSatsUnder3G = getDagSats(andelNedre, dagSatsFaktorUnder3G)
        val dagSatsOver3G = getDagSats(andelØvre, dagSatsFaktorOver3G)
        return (dagSatsUnder3G + dagSatsOver3G).setScale(0, RoundingMode.HALF_UP)
    }

    private fun getDagSats(grunnlag: BigDecimal, dagSatsFaktor: BigDecimal): BigDecimal = grunnlag.multiply(
        dagSatsFaktor,
        MathContext(
            antallDesimaler
        )
    )
}
