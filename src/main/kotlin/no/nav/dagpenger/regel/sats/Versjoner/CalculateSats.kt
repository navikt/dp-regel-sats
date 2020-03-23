package no.nav.dagpenger.regel.sats.Versjoner

import no.nav.dagpenger.regel.sats.Sats
import no.nav.dagpenger.regel.sats.SatsResult
import java.math.BigDecimal

val satsBeregner = Sats()

fun calculateSats(grunnlag: BigDecimal, antallBarn: Int): SatsResult {
    return satsBeregner.beregn(grunnlag, antallBarn)
}