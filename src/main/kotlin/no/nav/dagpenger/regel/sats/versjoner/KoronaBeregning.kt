package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.regel.sats.Beregning
import no.nav.dagpenger.regel.sats.Dekningsgrad
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.SatsResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month

// Innført av https://lovdata.no/dokument/LTI/forskrift/2020-03-20-368

class KoronaBeregning() : Beregning, BarnetilleggBeregning() {
    private val dagSatsFaktorUnder3G = Dekningsgrad(dekningsgrad = 80.0).getDagSatsFaktor()
    private val dagSatsFaktorOver3G = Dekningsgrad(dekningsgrad = 62.4).getDagSatsFaktor()

    override fun isActive(beregningstidspunkt: LocalDate): Boolean {
        val fom = LocalDate.of(2020, Month.MARCH, 20)
        val tom = LocalDate.of(2020, Month.DECEMBER, 31)
        val gyldighetsperiode = fom..tom

        return beregningstidspunkt in gyldighetsperiode
    }

    override fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult {
        val andelNedre = grunnlag.getGrunnlagMellom(0.0, 3.0)
        val andelØvre = grunnlag.getGrunnlagMellom(3.0, 6.0)

        val dagSatsUnder3G = getDagSats(andelNedre, dagSatsFaktorUnder3G)
        val dagSatsOver3G = getDagSats(andelØvre, dagSatsFaktorOver3G)

        val dagSats = dagSatsUnder3G + dagSatsOver3G

        return inkluderBarnetillegg(antallBarn, dagSats, grunnlag)
    }

    private fun getDagSats(grunnlag: BigDecimal, dagSatsFaktor: BigDecimal): BigDecimal =
        (grunnlag * dagSatsFaktor).setScale(
            0,
            RoundingMode.HALF_UP
        )
}