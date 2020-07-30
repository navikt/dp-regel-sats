package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Beregning
import no.nav.dagpenger.regel.sats.Beregningsregel
import no.nav.dagpenger.regel.sats.Dekningsgrad
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.SatsResult
import no.nav.dagpenger.regel.sats.antallDesimaler
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate

// Innført av https://lovdata.no/dokument/LTI/forskrift/2020-03-20-368

internal class KoronaBeregning() : Beregning, BarnetilleggBeregning() {
    private val REGEL_NAVN = Beregningsregel.KORONA

    private val dagSatsFaktorUnder3G = Dekningsgrad(dekningsgrad = 80.0).getDagSatsFaktor()
    private val dagSatsFaktorOver3G = Dekningsgrad(dekningsgrad = 62.4).getDagSatsFaktor()

    override fun isActive(beregningstidspunkt: LocalDate): Boolean = beregningstidspunkt.erKoronaPeriode()

    override fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult {
        val andelNedre = grunnlag.getGrunnlagMellom(0.0, 3.0)
        val andelØvre = grunnlag.getGrunnlagMellom(3.0, 6.0)

        val dagSatsUnder3G = getDagSats(andelNedre, dagSatsFaktorUnder3G)
        val dagSatsOver3G = getDagSats(andelØvre, dagSatsFaktorOver3G)
        val dagSats = (dagSatsUnder3G + dagSatsOver3G).setScale(0, RoundingMode.HALF_UP)

        return inkluderBarnetillegg(antallBarn, dagSats, grunnlag, REGEL_NAVN)
    }

    private fun getDagSats(grunnlag: BigDecimal, dagSatsFaktor: BigDecimal): BigDecimal = grunnlag.multiply(
        dagSatsFaktor,
        MathContext(
            antallDesimaler
        )
    )
}
